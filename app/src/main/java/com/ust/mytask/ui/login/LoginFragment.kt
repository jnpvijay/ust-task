package com.ust.mytask.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.ust.mytask.R
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import org.json.JSONObject

class LoginFragment : Fragment() {

    private lateinit var authService: AuthorizationService
    private val RC_AUTH = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authService = AuthorizationService(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.dispose()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startOAuthLogin()
        }
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
            Uri.parse("com.example.app:/oauth2redirect")
        )
            .setScopes("openid", "email", "profile")
            .build()
    }

    private fun startOAuthLogin() {
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

            val idToken = tokenResponse?.idToken
            val accessToken = tokenResponse?.accessToken
            val email = parseEmailFromIdToken(idToken)

            Log.d("AppAuth", "ID Token: $idToken")
            Log.d("AppAuth", "Access Token: $accessToken")
            Log.d("AppAuth", "Email: $email")
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
}
