package com.shady.fehlstunden;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.shady.listener.SwipeDismissListViewTouchListener;
import com.shady.logic.Constants;
import com.shady.logic.Seminar;
import com.shady.logic.Unit;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class SeminarView extends FragmentActivity {

    ListView listView;
    Activity mActivity;
    Seminar mSeminar;
    FloatingActionButton fab;
    MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seminar);
        mActivity = this;
        //Init FAB
        initFab();

        //Setup mSeminar from shared prefs with seminarId as filename
        String seminarId = getIntent().getStringExtra(Constants.KEY_SEMINAR_ID);
        initSeminarInstance(seminarId);

        //Set ActionBar title
        setTitle(mSeminar.getName());

        updateAttendanceScreen();

        listView = (ListView) findViewById(R.id.list_units);
        fillList(mSeminar.getUnits());
    }

    private void initFab() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "datePicker");

            }
        });
    }

    private void initSeminarInstance(String seminarId) {
        if (!seminarId.isEmpty()) {
            mSeminar = new Seminar();
            mSeminar.setId(UUID.fromString(seminarId));
            SharedPreferences pref = getSharedPreferences(mSeminar.getId().toString(), 0);
            mSeminar.setName(pref.getString(Constants.KEY_SEMINAR_NAME, "Kein Seminarname gefunden"));
            mSeminar.setMissed(pref.getInt(Constants.KEY_MISSED_VALUE, 0));
            mSeminar.setAllowedToMiss(pref.getInt(Constants.KEY_ALLOWED_TO_MISS_VALUE, 0));

            HashSet<String> unitSet = (HashSet<String>) pref.getStringSet(Constants.KEY_UNIT_LIST, new HashSet<String>());
            for (String s : unitSet) {
                Unit unit = new Unit();
                unit.setId(UUID.fromString(s));

                pref = getSharedPreferences(unit.getId().toString(), 0);
                String day = pref.getString(Constants.KEY_UNIT_DAY, "");
                String month = pref.getString(Constants.KEY_UNIT_MONTH, "");
                String year = pref.getString(Constants.KEY_UNIT_YEAR, "");

                unit.setDay(Integer.parseInt(day));
                unit.setMonth(Integer.parseInt(month));
                unit.setYear(Integer.parseInt(year));
                unit.setMissed(pref.getBoolean(Constants.KEY_UNIT_MISSED, false));
                mSeminar.addUnit(unit);
            }
        } else {
            Toast.makeText(this, "Seminar ID could not be resolved from intent", Toast.LENGTH_LONG).show();
        }
    }

    private void missUnit(Unit unit) {
        unit.setMissed(true);
        SharedPreferences prefs = getSharedPreferences(mSeminar.getId().toString(), 0);
        SharedPreferences.Editor editor = prefs.edit();

        int missed = mSeminar.getMissed();

        mSeminar.setMissed(missed+1);
        editor.remove(Constants.KEY_MISSED_VALUE).commit();
        editor.putInt(Constants.KEY_MISSED_VALUE, missed + 1).commit();
        updateAttendanceScreen();
    }

    private void unMissUnit(Unit unit) {
        unit.setMissed(false);
        SharedPreferences prefs = getSharedPreferences(mSeminar.getId().toString(), 0);
        SharedPreferences.Editor editor = prefs.edit();
        int missed = mSeminar.getMissed();

        if(missed > 0){
            mSeminar.setMissed(missed-1);
            editor.remove(Constants.KEY_MISSED_VALUE).commit();
            editor.putInt(Constants.KEY_MISSED_VALUE, missed - 1).commit();
        }else{
            mSeminar.setMissed(0);
            editor.remove(Constants.KEY_MISSED_VALUE).commit();
            editor.putInt(Constants.KEY_MISSED_VALUE, 0).commit();
        }
        updateAttendanceScreen();
    }

    private void updateAttendanceScreen() {
        TextView textView = (TextView) findViewById(R.id.textview_attendance);
        Integer x = mSeminar.getAllowedToMiss() - mSeminar.getMissed();
        if (x.equals(0)) {
            textView.setText("Du darfst NICHT mehr fehlen");
        } else if (x < 0) {
            textView.setText("Du hast schon " + Math.abs(x) + " mal zu oft gefehlt");
        } else {
            textView.setText("Du darfst noch " + x + " mal fehlen");
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.seminar, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillList(final List<Unit> units) {
        mAdapter = new MyAdapter(this, units);
        listView.setAdapter(mAdapter);

        //Swipe to REMOVE units
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView, new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(ListView listView, final int[] reverseSortedPositions) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder
                        .setMessage("Wirklich löschen?")
                        .setTitle("Einheit entfernen")
                        .setCancelable(false)
                        .setPositiveButton("Ja, löschen!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int x : reverseSortedPositions) {
                                    Unit unitToRemove = units.remove(x);
                                    mSeminar.setUnits(units);

                                    if (unitToRemove.isMissed()) {
                                        unMissUnit(unitToRemove);
                                    }

                                    //Remove mSeminar from main shared preferences
                                    SharedPreferences prefs = getSharedPreferences(mSeminar.getId().toString(), 0);
                                    SharedPreferences.Editor editor = prefs.edit();

                                    editor.remove(Constants.KEY_MISSED_VALUE).commit();
                                    editor.putInt(Constants.KEY_MISSED_VALUE, mSeminar.getMissed()).commit();
                                    HashSet<String> unitSet = (HashSet<String>) prefs.getStringSet(Constants.KEY_UNIT_LIST, new HashSet<String>());
                                    unitSet.remove(unitToRemove.getId().toString());

                                    editor.putStringSet(Constants.KEY_UNIT_LIST, unitSet).commit();

                                    prefs = getSharedPreferences(unitToRemove.getId().toString(), 0);
                                    prefs.edit().clear().commit();
                                    mAdapter.notifyDataSetChanged();
                                }
                                updateAttendanceScreen();
                            }
                        })
                        .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }).create().show();
            }
        });

        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener(touchListener.makeScrollListener());
    }

    private class MyAdapter extends BaseAdapter {
        private Activity mContext;
        private List<Unit> mList;
        private LayoutInflater mLayoutInflater = null;

        public MyAdapter(Activity context, List<Unit> list) {
            mContext = context;
            this.mList = list;
            mLayoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Unit unit = mList.get(position);

            View v = convertView;
            final MyAdapterViewHolder viewHolder;

            if (convertView == null) {
                v = mLayoutInflater.inflate(R.layout.item_unit, null);
                viewHolder = new MyAdapterViewHolder(v);
                v.setTag(viewHolder);
            } else {
                viewHolder = (MyAdapterViewHolder) v.getTag();
            }

            viewHolder.columnName.setText(unit.getDay() + "/" + (unit.getMonth()+1) + "/" + unit.getYear());
//            columnAttendance.setText(item.getMissed() + "");


            //Auslesen ob unit verpasst wurde
            viewHolder.checkBox.setChecked(false);
            SharedPreferences pref = getSharedPreferences(unit.getId().toString(), 0);
            if (pref.getBoolean(Constants.KEY_UNIT_MISSED, false)) {
                viewHolder.checkBox.setChecked(true);
            }


            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int missedValue = mSeminar.getMissed();

                    //TODO Gibt es eine bessere Lösung statt hier Logik einzubauen?
                    if (viewHolder.checkBox.isChecked()) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                        builder
                                .setMessage("Hast du wirklich gefehlt?")
                                .setTitle("Sicher?")
                                .setCancelable(false)
                                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        missUnit(unit);

                                        //Set mSeminar-missed int value in mSeminar prefs
                                        SharedPreferences pref = getSharedPreferences(mSeminar.getId().toString(), 0);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putInt(Constants.KEY_MISSED_VALUE, mSeminar.getMissed());
                                        editor.commit();

                                        //Set unit to missed in unit prefs
                                        pref = getSharedPreferences(unit.getId().toString(), 0);
                                        editor = pref.edit();
                                        editor.putBoolean(Constants.KEY_UNIT_MISSED, true);
                                        editor.commit();
                                    }
                                })
                                .setNegativeButton("Nein, docht nicht", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        viewHolder.checkBox.setChecked(false);
                                        updateAttendanceScreen();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        viewHolder.checkBox.setChecked(false);
                                        updateAttendanceScreen();
                                    }
                                });
                        builder.create().show();
                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("Du warst doch anwesend?").setTitle("Sicher?")
                                .setCancelable(false)
                                .setPositiveButton("Ja, war da", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        unMissUnit(unit);

                                        //Set mSeminar-missed int value in mSeminar prefs
                                        SharedPreferences pref = getSharedPreferences(mSeminar.getId().toString(), 0);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putInt(Constants.KEY_MISSED_VALUE, mSeminar.getMissed());
                                        editor.commit();

                                        //Set unit to missed in unit prefs
                                        pref = getSharedPreferences(unit.getId().toString(), 0);
                                        editor = pref.edit();
                                        editor.putBoolean(Constants.KEY_UNIT_MISSED, false);
                                        editor.commit();

                                    }
                                })
                                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        viewHolder.checkBox.setChecked(true);
                                        updateAttendanceScreen();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        viewHolder.checkBox.setChecked(true);
                                        updateAttendanceScreen();
                                    }
                                });
                        builder.create().show();
                    }
                }
            });

            return v;
        }
    }

    private class MyAdapterViewHolder {
        public TextView columnName;
        public TextView columnAttendance;
        public CheckBox checkBox;

        public MyAdapterViewHolder(View base) {
            columnName = (TextView) base.findViewById(R.id.column_name);
            columnAttendance = (TextView) base.findViewById(R.id.column_attendance);
            checkBox = (CheckBox) base.findViewById(R.id.checkbox);
        }
    }

    //For adding new units
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        Seminar _seminar;
        MyAdapter _adapter;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            SeminarView seminarActivity = (SeminarView) getActivity();

            _seminar = seminarActivity.mSeminar;
            _adapter = seminarActivity.mAdapter;

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Unit unit = new Unit();
            unit.setId(UUID.randomUUID());

            unit.setDay(day);
            unit.setMonth(month);
            unit.setYear(year);

            List<Unit> units = _seminar.addUnit(unit);

            SharedPreferences pref = view.getContext().getSharedPreferences(_seminar.getId().toString(), 0);
            SharedPreferences.Editor editor = pref.edit();

            Set<String> unitSet = new HashSet<>();
            for (Unit u : units) {
                unitSet.add(u.getId().toString());
            }
            editor.putStringSet(Constants.KEY_UNIT_LIST, new HashSet<>(unitSet));
            editor.commit();

            pref = view.getContext().getSharedPreferences(unit.getId().toString(), 0);
            editor = pref.edit();
            editor.putString(Constants.KEY_UNIT_DAY, String.valueOf(unit.getDay()));
            editor.putString(Constants.KEY_UNIT_MONTH, String.valueOf(unit.getMonth()));
            editor.putString(Constants.KEY_UNIT_YEAR, String.valueOf(unit.getYear()));
            editor.commit();

            _adapter.notifyDataSetChanged();
        }
    }
}
