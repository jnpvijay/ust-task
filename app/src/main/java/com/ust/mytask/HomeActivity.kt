package com.ust.mytask

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ust.mytask.ui.detail.DetailFragment
import com.ust.mytask.ui.home.HomeFragment
import com.ust.mytask.ui.login.LoginFragment
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {

    private lateinit var authService: AuthorizationService
    private val RC_AUTH = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authService = AuthorizationService(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        if (loadAuthState() != null) {
            if (isConnected(this)) {
                trySilentLogin()
            } else {
                clearAuthState()
                navigateLoginScreen()
            }
        } else {
            navigateLoginScreen()
        }

    }


    private fun navigateLoginScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, LoginFragment())
            .commit()
    }

    private fun navigateHomeScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, HomeFragment())
            .commit()
    }

    public fun navigateDetailScreen() {
        supportFragmentManager.beginTransaction()
            .add(R.id.frameContainer, DetailFragment())
            .addToBackStack("detail")
            .commit()
    }


    // ------------------ OAuth Logic -----------------------

    private fun buildAuthRequest(): AuthorizationRequest {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
            Uri.parse("https://oauth2.googleapis.com/token")
        )

        return AuthorizationRequest.Builder(
            serviceConfig,
            getString(R.string.web_client_id),
            ResponseTypeValues.CODE,
            Uri.parse("com.ust.mytask:/oauth2redirect")
        )
            .setScopes("openid", "email", "profile")
            .build()
    }

    public fun startOAuthLogin() {
        val authRequest = buildAuthRequest()
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        startActivityForResult(authIntent, RC_AUTH)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            val resp = AuthorizationResponse.fromIntent(data!!)
            val err = AuthorizationException.fromIntent(data)

            if (resp != null) {
                exchangeCode(resp)
            } else {
                Log.e("AppAuth", "Authorization error: $err")
            }
        }
    }

    private fun exchangeCode(authResponse: AuthorizationResponse) {

        val tokenRequest = authResponse.createTokenExchangeRequest()

        authService.performTokenRequest(tokenRequest) { tokenResponse, ex ->
            if (ex != null) {
                Log.e("AppAuth", "Token exchange failed: $ex")
                return@performTokenRequest
            }

            if (tokenResponse != null) {
                val authState = AuthState(authResponse, tokenResponse, ex)
                saveAuthState(authState)


                val idToken = tokenResponse?.idToken
                val accessToken = tokenResponse?.accessToken
                val email = parseEmailFromIdToken(idToken)

                Log.d("AppAuth", "ID Token: $idToken")
                Log.d("AppAuth", "Access Token: $accessToken")
                Log.d("AppAuth", "Email: $email")

                //val email = parseEmailFromIdToken(tokenResponse.idToken)
                //Log.d("AppAuth", "First Login: $email")

                navigateHomeScreen()
            }
        }
    }

    private fun parseEmailFromIdToken(idToken: String?): String? {
        if (idToken == null) return null
        val parts = idToken.split(".")
        if (parts.size < 2) return null

        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val json = JSONObject(payload)
        return json.optString("email")
    }

    private fun saveAuthState(state: AuthState) {
        val json = state.jsonSerializeString()
        this.getSharedPreferences("auth", 0)
            .edit()
            .putString("state", json)
            .apply()
    }

    private fun loadAuthState(): AuthState? {
        val json = this.getSharedPreferences("auth", 0)
            .getString("state", null)

        return if (json != null) AuthState.jsonDeserialize(json) else null
    }

    private fun trySilentLogin() {
        val state = loadAuthState() ?: return  // not logged in before

        state.performActionWithFreshTokens(authService) { accessToken, idToken, ex ->
            if (ex != null) {
                Log.e("SilentAuth", "Silent login failed: $ex")
                return@performActionWithFreshTokens
            }

            val email = parseEmailFromIdToken(idToken)
            Log.d("SilentAuth", "Silent login success: $email")

            navigateHomeScreen()
        }
    }

    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun clearAuthState() {
        getSharedPreferences("auth", Context.MODE_PRIVATE)
            .edit()
            .remove("state")
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (authService != null)
            authService.dispose()
    }
}