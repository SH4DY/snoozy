package com.shady.fehlstunden;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.shady.listener.SwipeDismissListViewTouchListener;
import com.shady.logic.Constants;
import com.shady.logic.Seminar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class Main extends Activity {

    Activity mActivity;
    ListView lv;
    List<Seminar> seminarList = new ArrayList<Seminar>();
    ArrayAdapter adapter;
    FloatingActionButton fab;
    static final int TUTORIAL_FIRST_STARTUPS = 2;
    ShowcaseView displayedTutorialView;
    public RelativeLayout.LayoutParams tutButtonLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        //Init Seminar List
        updateSeminarList();

        //FAB to add units
        initFab();

        //check For First x startup(s)
        checkForFirstStartups();
    }

    //Show tutorial the first x startups
    private void checkForFirstStartups() {
        SharedPreferences prefs = getPreferences(0);
        SharedPreferences.Editor editor = prefs.edit();
        int timesStarted = prefs.getInt(Constants.KEY_STARTED_TIMES, 0);

        if (timesStarted <= TUTORIAL_FIRST_STARTUPS) {
            timesStarted++;
            editor.putInt(Constants.KEY_STARTED_TIMES, timesStarted);
            editor.commit();

            //show tutorial
            showTutorialAddSeminar();
        }
    }

    private void showTutorialAddSeminar() {
        tutButtonLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tutButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tutButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        tutButtonLayout.setMargins(margin, margin, margin, margin * 15);

        displayedTutorialView = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.fab, mActivity))
                .setContentTitle("\n \n \n Los geht's!")
                .setContentText("\n \n \n \n \n Hier unten kannst du LV's hinzufügen")
                .hideOnTouchOutside()
                .setStyle(R.style.TutorialStyle1)
                .build();
        displayedTutorialView.setButtonPosition(tutButtonLayout);
    }

    private void showTutorialDeleteSeminar() {
        if (displayedTutorialView != null) {
            displayedTutorialView.hide();
            displayedTutorialView = new ShowcaseView.Builder(this)
                    .setTarget(Target.NONE)
                    .setContentText("\n \n \n \n \n \n \n \n \nWisch über ein Seminar um es zu löschen!" +
                            "\n\nDrück lange auf ein Seminar um es zu bearbeiten!")
                    .hideOnTouchOutside()
                    .setStyle(R.style.TutorialStyle1)
                    .build();
            displayedTutorialView.setButtonPosition(tutButtonLayout);

        }
    }

    private void initFab() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(mActivity);
                View promptsView = li.inflate(R.layout.seminar_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                final NumberPicker picker = (NumberPicker) promptsView.findViewById(R.id.numberPicker);
                picker.setMaxValue(20);
                picker.setMinValue(0);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        String seminarName = userInput.getText().toString();

                                        Seminar seminar = new Seminar();
                                        seminar.setName(seminarName);
                                        seminar.setId(UUID.randomUUID());
                                        seminarList.add(seminar);

                                        SharedPreferences pref = getPreferences(0);
                                        SharedPreferences.Editor editor = pref.edit();

                                        Set<String> set = new HashSet<String>();

                                        for (Seminar sem : seminarList) {
                                            set.add(sem.getId().toString());
                                        }

                                        //Add mSeminar ID to general shared prefs
                                        editor.putStringSet(Constants.KEY_SEMINAR_LIST, set);
                                        editor.commit();

                                        //Set mSeminar name and allowed-to-miss value in mSeminar shared prefs
                                        pref = getSharedPreferences(seminar.getId().toString(), 0);
                                        editor = pref.edit();
                                        editor.putInt(Constants.KEY_ALLOWED_TO_MISS_VALUE, picker.getValue());
                                        editor.putString(Constants.KEY_SEMINAR_NAME, seminar.getName());
                                        editor.commit();

                                        updateSeminarList();

                                        showTutorialDeleteSeminar();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });
    }

    private void updateSeminarList() {
        lv = (ListView) findViewById(R.id.list_units);
        final SharedPreferences pref = getPreferences(0);
        seminarList.clear();

        //Add all seminars from shared prefs to seminarList
        for (String s : pref.getStringSet(Constants.KEY_SEMINAR_LIST, new HashSet<String>())) {
            Seminar seminar = new Seminar();

            SharedPreferences prefs = getSharedPreferences(s, 0);
            String seminarName = prefs.getString(Constants.KEY_SEMINAR_NAME, "");
            seminar.setName(seminarName);
            seminar.setId(UUID.fromString(s));
            seminarList.add(seminar);
        }

        if (adapter == null) {
            adapter = new ArrayAdapter<Seminar>(this, R.layout.item_seminar, seminarList);
            lv.setAdapter(adapter);


            //Single click ot enter detailed SeminarView
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Seminar seminar = seminarList.get(position);
                    Intent intent = new Intent(view.getContext(), SeminarView.class);
                    intent.putExtra(Constants.KEY_SEMINAR_ID, seminar.getId().toString());

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity, findViewById(R.id.list_units), "list");
                    startActivity(intent, options.toBundle());
                }
            });

            //Longclick to EDIT seminars
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    LayoutInflater li = LayoutInflater.from(mActivity);
                    View promptsView = li.inflate(R.layout.seminar_prompt, null);

                    final Seminar seminar = seminarList.get(position);

                    final EditText userInput = (EditText) promptsView
                            .findViewById(R.id.editTextDialogUserInput);

                    final NumberPicker picker = (NumberPicker) promptsView.findViewById(R.id.numberPicker);
                    picker.setMaxValue(20);
                    picker.setMinValue(0);

                    final SharedPreferences prefs = getSharedPreferences(seminar.getId().toString(), 0);
                    userInput.setText(prefs.getString(Constants.KEY_SEMINAR_NAME, ""));

                    picker.setValue(prefs.getInt(Constants.KEY_ALLOWED_TO_MISS_VALUE, 0));
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder
                            .setView(promptsView)
                            .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.remove(Constants.KEY_SEMINAR_NAME);
                                    editor.commit();

                                    seminar.setName(userInput.getText().toString());
                                    editor.putString(Constants.KEY_SEMINAR_NAME, seminar.getName());
                                    editor.commit();

                                    editor.remove(Constants.KEY_ALLOWED_TO_MISS_VALUE);
                                    editor.commit();

                                    seminar.setAllowedToMiss(picker.getValue());
                                    editor.putInt(Constants.KEY_ALLOWED_TO_MISS_VALUE, seminar.getAllowedToMiss());
                                    editor.commit();

                                    updateSeminarList();
                                }
                            })
                            .create().show();

                    return false;
                }
            });

            SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(lv, new SwipeDismissListViewTouchListener.DismissCallbacks() {
                @Override
                public boolean canDismiss(int position) {
                    return true;
                }

                @Override
                public void onDismiss(ListView listView, final int[] reverseSortedPositions) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder
                            .setMessage("Wirklich löschen?")
                            .setTitle("Sicher?")
                            .setCancelable(false)
                            .setPositiveButton("Ja, löschen!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (int x : reverseSortedPositions) {
                                        Seminar semToRemove = seminarList.remove(x);
                                        //Remove mSeminar from main shared preferences
                                        SharedPreferences prefs = getPreferences(0);
                                        SharedPreferences.Editor editor = prefs.edit();

                                        HashSet<String> seminarSet = (HashSet<String>) prefs.getStringSet(Constants.KEY_SEMINAR_LIST, new HashSet<String>());

                                        for (String s : seminarSet) {
                                            if (semToRemove.getId().toString().equals(s)) {
                                                seminarSet.remove(s);
                                                break;
                                            }
                                        }
                                        editor.remove(Constants.KEY_SEMINAR_LIST).commit();
                                        editor.putStringSet(Constants.KEY_SEMINAR_LIST, seminarSet);
                                        editor.commit();

                                        //Clear mSeminar shared pref file
                                        prefs = getSharedPreferences(semToRemove.getId().toString(), 0);
                                        prefs.edit().clear().commit();
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            })
                            .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.notifyDataSetChanged();
                                }
                            }).create().show();
                }
            });

            lv.setOnTouchListener(touchListener);
            lv.setOnScrollListener(touchListener.makeScrollListener());
        }
        adapter.notifyDataSetChanged();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
}

