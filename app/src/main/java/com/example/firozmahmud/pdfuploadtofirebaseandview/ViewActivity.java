package com.example.firozmahmud.pdfuploadtofirebaseandview;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.barteksc.pdfviewer.PDFView;

import es.voghdev.pdfviewpager.library.RemotePDFViewPager;
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter;
import es.voghdev.pdfviewpager.library.remote.DownloadFile;

public class ViewActivity extends AppCompatActivity {


    private String pdfUri;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        getPdf();
        initVariable();

        viewPdf();
    }

    private void viewPdf() {

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://docs.google.com/viewer?url="+pdfUri);
    }

    private void initVariable() {


    }


    private void initView() {
        setContentView(R.layout.activity_view);

        webView = findViewById(R.id.webView);
    }

    private void getPdf() {

        pdfUri = getIntent().getStringExtra("pdf");

    }

}
