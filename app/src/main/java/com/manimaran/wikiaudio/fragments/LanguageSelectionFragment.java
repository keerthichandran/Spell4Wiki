package com.manimaran.wikiaudio.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapters.LangAdapter;
import com.manimaran.wikiaudio.constants.EnumTypeDef.LanguageSelectionMode;
import com.manimaran.wikiaudio.databases.DBHelper;
import com.manimaran.wikiaudio.databases.entities.WikiLang;
import com.manimaran.wikiaudio.listerners.OnLanguageSelectionListener;
import com.manimaran.wikiaudio.utils.PrefManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class LanguageSelectionFragment extends BottomSheetDialogFragment {

    private PrefManager pref;
    private OnLanguageSelectionListener callback;
    private List<WikiLang> wikiLanguageList = new ArrayList<>();
    private LangAdapter adapter;
    @LanguageSelectionMode
    private int languageSelectionMode;
    private String preSelectedLanguageCode = null;

    public LanguageSelectionFragment() {
    }

    public void init(OnLanguageSelectionListener callback, @LanguageSelectionMode int mode) {
        init(callback, mode, null);
    }

    public void init(OnLanguageSelectionListener callback, @LanguageSelectionMode int mode, String preSelectedLanguageCode) {
        this.callback = callback;
        this.languageSelectionMode = mode;
        this.preSelectedLanguageCode = preSelectedLanguageCode;
        setCancelable(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        pref = new PrefManager(getContext());
        if (TextUtils.isEmpty(preSelectedLanguageCode))
            preSelectedLanguageCode = getExistingLanguageCode();

        final BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), R.style.AppTheme);

        dialog.setContentView(R.layout.bottom_sheet_language_selection);

        final ListView listView = dialog.findViewById(R.id.list_view_lang);
        ImageView btnClose = dialog.findViewById(R.id.btn_close);
        final SearchView searchView = dialog.findViewById(R.id.search_view);

        DBHelper dbHelper = new DBHelper(getContext());

        /*
         * Check Wiktionary mode or not
         * If wiktionary mode show all languages
         * If Contribution mode show only language have "title_words_without_audio" key-value
         * "title_words_without_audio" - category of words without audio in wiktionary
         */
        wikiLanguageList.clear();
        if (languageSelectionMode == LanguageSelectionMode.SPELL_4_WIKI) {
            wikiLanguageList = dbHelper.getAppDatabase().getWikiLangDao().getWikiLanguageListForWordsWithoutAudio();
        } else {
            wikiLanguageList = dbHelper.getAppDatabase().getWikiLangDao().getWikiLanguageList();
        }

        OnLanguageSelectionListener languageSelectionListener = langCode -> {
            switch (languageSelectionMode) {
                case LanguageSelectionMode.SPELL_4_WIKI:
                    pref.setLanguageCodeSpell4Wiki(langCode);
                    break;
                case LanguageSelectionMode.SPELL_4_WORD_LIST:
                    pref.setLanguageCodeSpell4WordList(langCode);
                    break;
                case LanguageSelectionMode.SPELL_4_WORD:
                    pref.setLanguageCodeSpell4Word(langCode);
                    break;
                case LanguageSelectionMode.WIKTIONARY:
                    pref.setLanguageCodeWiktionary(langCode);
                    break;
                case LanguageSelectionMode.TEMP:
                    break;
            }

            if (callback != null)
                callback.OnCallBackListener(langCode);
            dismiss();
        };

        adapter = new LangAdapter(getActivity(), wikiLanguageList, languageSelectionListener, preSelectedLanguageCode);
        if (listView != null) {
            listView.setAdapter(adapter);
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(view -> dismiss());
        }

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setHideable(false);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                // Full screen mode no collapse
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int screenHeight = displaymetrics.heightPixels;
                behavior.setPeekHeight(screenHeight);
            }

        });

        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search_here));
            searchView.setQueryRefinementEnabled(true);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (adapter != null)
                        adapter.getFilter().filter(newText);
                    return false;
                }
            });
        }
        return dialog;
    }

    private String getExistingLanguageCode() {
        switch (languageSelectionMode) {
            case LanguageSelectionMode.SPELL_4_WIKI:
                return pref.getLanguageCodeSpell4Wiki();
            case LanguageSelectionMode.SPELL_4_WORD_LIST:
                return pref.getLanguageCodeSpell4WordList();
            case LanguageSelectionMode.SPELL_4_WORD:
                return pref.getLanguageCodeSpell4Word();
            case LanguageSelectionMode.WIKTIONARY:
                return pref.getLanguageCodeWiktionary();
            case LanguageSelectionMode.TEMP:
            default:
                return null;
        }
    }

    @Override
    public void onCancel(@NotNull DialogInterface dialog) {
        super.onCancel(dialog);

    }
}