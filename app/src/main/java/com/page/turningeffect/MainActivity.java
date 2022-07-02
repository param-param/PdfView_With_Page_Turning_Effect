package com.page.turningeffect;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.content.res.AssetManager;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static int totalpages;
    private static ViewPager viewPager;
    private AppCompatSeekBar seekBar;
    private TextView tvMin, tvMax;
    private static int currentPage = 1;
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initViews();
        initListeners();
        showPDF();
    }



    /////////////////////////////////////////////////////////////
    // Initial Methods
    /////////////////////////////////////////////////////////////

    public void initViews() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tvMax = findViewById(R.id.tvMax);
        tvMin = findViewById(R.id.tvMin);
        seekBar = findViewById(R.id.seekBar);
    }


    public void initListeners() {
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                seekBar.setProgress(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    /////////////////////////////////////////////////////////////
    // Method to set page curl effect on ViewPager
    /////////////////////////////////////////////////////////////
    public void showPDF() {
        try {
            totalpages = countPages(fileFromAssetsFolder());
            seekBar.setMax(totalpages - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        viewPager.setPageTransformer(false, new PDFTestFragment.PageCurlPageTransformer());
        viewPager.setOffscreenPageLimit(0);

    }


    /////////////////////////////////////////////////////////////
    // Method to Count number of pages in the pdf provided
    /////////////////////////////////////////////////////////////
    private int countPages(File pdfFile) throws IOException {
        try {

            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                totalpages = pdfRenderer.getPageCount();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return totalpages;
    }


    /////////////////////////////////////////////////////////////
    // Method to return pdf file from assets folder
    /////////////////////////////////////////////////////////////
    private File fileFromAssetsFolder() {
        AssetManager assetManager = getAssets();
        File outFile = null;
        InputStream in = null;
        OutputStream out = null;
        String newFileName = "newFileName";

        try {
            in = assetManager.open(PDFTestFragment.SAMPLE_FILE);
            outFile = new File(getExternalFilesDir(null), newFileName);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
        } catch (IOException e) {

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }

        return outFile;
    }


    /////////////////////////////////////////////////////////////
    // Method to copy Pdf file from assets folder
    /////////////////////////////////////////////////////////////
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    /////////////////////////////////////////////////////////////
    // Seek bar change listener
    /////////////////////////////////////////////////////////////
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvMax.setText(String.valueOf(totalpages - 1));
            tvMin.setText(String.valueOf(progress));
            viewPager.setCurrentItem(progress);

            count = 0;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };


    /////////////////////////////////////////////////////////////
    // Call Backs
    /////////////////////////////////////////////////////////////
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    /////////////////////////////////////////////////////////////
    // Adapter
    /////////////////////////////////////////////////////////////
    public static class PagerAdapter extends FragmentStatePagerAdapter {
        public Map<Integer, PDFTestFragment> mPageReferenceMap;

        PagerAdapter(FragmentManager fm) {
            super(fm);
            mPageReferenceMap = new HashMap<Integer, PDFTestFragment>();
        }

        @Override
        public Fragment getItem(int position) {
            currentPage = position;
            PDFTestFragment testFragment = PDFTestFragment.getInstance(position);
            mPageReferenceMap.put(position, testFragment);
            return testFragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            mPageReferenceMap.remove(position);
        }

        @Override
        public int getCount() {
            return totalpages;
        }

        public PDFTestFragment getFragment(int key) {
            return mPageReferenceMap.get(key);
        }
    }


    /////////////////////////////////////////////////////////////
    // Fragment
    /////////////////////////////////////////////////////////////
    public static class PDFTestFragment extends Fragment implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener,
            OnErrorListener{

        private int mPage;
        private PDFView pdfView;
        private LinearLayoutCompat llSeekbar;
        public static final String SAMPLE_FILE = "monkey_story.pdf";
        Integer pageNumber = 0;
        ProgressBar progress_bar;
        TextView mintv, maxtv;

        public static PDFTestFragment getInstance(int page) {
            PDFTestFragment pdfTestFragment = new PDFTestFragment();
            Bundle args = new Bundle();
            args.putInt("page", page);
            pdfTestFragment.setArguments(args);
            return pdfTestFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPage = getArguments().getInt("page");
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_test, container, false);
            progress_bar = getActivity().findViewById(R.id.progress_bar);
            mintv = getActivity().findViewById(R.id.tvMin);
            maxtv = getActivity().findViewById(R.id.tvMax);
            pdfView = (PDFView) view.findViewById(R.id.pdfView);
            llSeekbar = view.findViewById(R.id.llSeekbar);

            displayPdfFromAssets(mPage);

            view.setTag(R.id.viewPager, mPage);

            return view;
        }


        private void displayPdfFromAssets(int pageNumberToShow) {

            pdfView.fromAsset(SAMPLE_FILE)
                    .pages(pageNumberToShow)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .onPageChange(this)
                    .onTap(new OnTapListener() {
                        @Override
                        public boolean onTap(MotionEvent e) {
                            return true;
                        }
                    })
                    .enableAnnotationRendering(true)
                    .onLoad(this)
                    .scrollHandle(null)
                    .load();
        }


        @Override
        public void onPageChanged(int page, int pageCount) {
            pageNumber = page;
        }


        @Override
        public void loadComplete(int nbPages) {
            progress_bar.setVisibility(View.GONE);

        }

        @Override
        public void onPageError(int page, Throwable t) {
            progress_bar.setVisibility(View.GONE);
        }

        @Override
        public void onError(Throwable t) {
            progress_bar.setVisibility(View.GONE);
        }


        public static class PageCurlPageTransformer implements ViewPager.PageTransformer {

            @Override
            public void transformPage(View page, float position) {

                Log.d("TAG", "transformPage, position = " + position + ", page = " + page.getTag(R.id.viewPager));
                if (page instanceof PageCurl) {
                    if (position > -1.0F && position < 1.0F) {
                        // hold the page steady and let the views do the work
                        page.setTranslationX(-position * page.getWidth());
                    } else {
                        page.setTranslationX(0.0F);
                    }
                    if (position <= 1.0F && position >= -1.0F) {
                        ((PageCurl) page).setCurlFactor(position);
                    }
                }
            }
        }

    }


}


