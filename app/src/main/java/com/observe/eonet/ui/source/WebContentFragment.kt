package com.observe.eonet.ui.source

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.observe.eonet.R
import com.observe.eonet.firebase.AnalyticsManager
import com.observe.eonet.util.visible
import kotlinx.android.synthetic.main.web_content_fragment.*

class WebContentFragment : Fragment() {

    private lateinit var viewModel: WebContentViewModel
    private val args: WebContentFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AnalyticsManager.reportScreenViewEvent("source")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(WebContentViewModel::class.java)
        return inflater.inflate(R.layout.web_content_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visible = false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visible = true
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?,
                                            error: SslError?) {
                val dialogBuilder = AlertDialog.Builder(context!!)
                dialogBuilder.setMessage("SSL certificate validation failed")
                dialogBuilder.setPositiveButton("continue") { _, _ -> handler?.proceed() }
                dialogBuilder.setNegativeButton("cancel") { _, _ -> handler?.proceed() }
                dialogBuilder.show()
            }
        }

        webview.loadUrl(args.contentUrl)
    }
}
