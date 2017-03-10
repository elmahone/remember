package com.example.remember.fragment;


import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.remember.AlarmReceiver;
import com.example.remember.R;
import com.example.remember.activity.MainActivity;
import com.example.remember.adapter.ShoppingListAdapter;
import com.example.remember.database.DataSource;
import com.example.remember.model.Reminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CategoryShoppingFragment extends Fragment {
    DataSource dataSource;
    int catId;
    InputMethodManager inputManager;
    Calendar calendar;
    ShoppingListAdapter adapter;

    List<String> values = new ArrayList<>();
    EditText title;
    Button addRow;
    EditText date;
    EditText newListItem;
    ListView shoppingList;
    String newItem;

    public CategoryShoppingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category_shopping, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dataSource = new DataSource(getActivity());
        catId = getArguments().getInt("category");
        findViews();
        adapter = new ShoppingListAdapter(getActivity(), values, true);
        shoppingList.setAdapter(adapter);

        //region DatePicker & TimePicker listeners
        final DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        final TimePickerDialog.OnTimeSetListener timePicker = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                new DatePickerDialog(getActivity(), datePicker,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        };
        //endregion

        // Date click listener
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                closeKeyboard();
                new TimePickerDialog(getActivity(), timePicker,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true).show();
            }
        });
        addRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newItem = newListItem.getText().toString();
                if (!newItem.matches("")) {
                    Toast.makeText(getActivity(), newItem, Toast.LENGTH_SHORT).show();
                    adapter.addItem(newItem);
                    newListItem.setText("");
                    closeKeyboard();
                } else {
                    Toast.makeText(getActivity(), "Type something first", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveReminder();
                Log.v("ITEMS", adapter.getItems().toString());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findViews() {
        title = (EditText) getView().findViewById(R.id.edit_reminder_title);
        date = (EditText) getView().findViewById(R.id.edit_reminder_date);
        newListItem = (EditText) getView().findViewById(R.id.new_list_item);
        shoppingList = (ListView) getView().findViewById(R.id.shopping_list);
        addRow = (Button) getView().findViewById(R.id.add_new_list_item);
    }

    // Shopping list doesn't require date?

    private void saveReminder() {
        String t = title.getText().toString();
        String da = date.getText().toString();
        List<String> shopping = adapter.getItems();
        if (!t.matches("") && !da.matches("")) {
            Reminder reminder = new Reminder(t, shopping, catId, calendar.getTimeInMillis());
            int id = (int) dataSource.createReminder(reminder);
            dataSource.close();
            setAlarm(calendar, reminder, id);

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Reminder needs a title and a date", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void updateLabel() {
        String format = "dd.MM.yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        date.setText(sdf.format(calendar.getTime()));
    }

    private void setAlarm(Calendar targetCal, Reminder reminder, int id) {
        Toast.makeText(getActivity(), "Alarm is set", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.putExtra("reminder", reminder);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), id, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }


}
