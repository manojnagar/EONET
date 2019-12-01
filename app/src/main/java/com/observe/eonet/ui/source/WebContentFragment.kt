package com.observe.eonet.ui.source

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.observe.eonet.R
import com.observe.eonet.util.visible
import kotlinx.android.synthetic.main.web_content_fragment.*

class WebContentFragment : Fragment() {

    private lateinit var viewModel: WebContentViewModel
    private val args: WebContentFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(WebContentViewModel::class.java)
        return inflater.inflate(R.layout.web_content_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(
                view: WebView,
                url: String
            ) {
                progressBar.visible = false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visible = true
            }
        }

        webview.loadUrl(args.contentUrl)
    }
}
