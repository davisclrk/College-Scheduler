package com.example.a2340project1.ui.agenda;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.a2340project1.R;
import com.example.a2340project1.ui.DynamicElementHandler;
import com.example.a2340project1.ui.classes.ClassElementHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

/**
 * Handles adding, removing, and editing dynamically added assignments and exams to the
 * agenda fragment. Implementations of these actions are the same as in
 * DynamicElementHandler.
 *
 * @author aidannguyen
 * @version 1.0
 */
public class AgendaElementHandler extends DynamicElementHandler {
    private LayoutInflater inflater;

    private int displayDay, displayMonth, displayYear;
    private ArrayList<AgendaElement> AgendaElements = new ArrayList<>();
    private boolean sortByClass = false;
    private int showElements = 0; // 0 == all, 1 == assignments, 2 == exams

    /**
     * A version of the superclass showEditDialog method, but with extra variables for the
     * different input fields. Also adds a new class to the context.
     *
     * @see DynamicElementHandler#showEditDialog(String, ViewGroup, LayoutInflater, View, Context, int, int, int)
     */
    public void agendaAddDialog(ViewGroup viewGroup, LayoutInflater inflater, Context context,
                                DatePickerDialog.OnDateSetListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogBoxTheme);

        // set the custom layout
        View customLayout = inflater.inflate(R.layout.agenda_popup_dialog, null);
        builder.setView(customLayout);
        AlertDialog dialog = builder.create();

        Button addAssignment = customLayout.findViewById(R.id.add_assignment);
        addAssignment.setOnClickListener(v -> agendaAddAssignment(inflater, context, listener, viewGroup, dialog));

        Button addExam = customLayout.findViewById(R.id.add_exam);
        addExam.setOnClickListener(v -> agendaAddExam(inflater, context, listener, viewGroup, dialog));

        dialog.show();
    }

    /**
     * Shows dialog for adding a new assignment.
     *
     * @param inflater the LayoutInflater
     * @param context the context of the destination fragment
     * @param listener the onDateSet listener
     * @param viewGroup the ViewGroup of AgendaFragment
     * @param agendaDialog the dialog of the agendaAddDialog method
     */
    private void agendaAddAssignment(LayoutInflater inflater, Context context,
                                     DatePickerDialog.OnDateSetListener listener,
                                     ViewGroup viewGroup, AlertDialog agendaDialog) {
        AlertDialog.Builder assignmentBuilder = new AlertDialog.Builder(context, R.style.DialogBoxTheme);
        assignmentBuilder.setTitle("Add Assignment");

        View assignmentLayout = inflater.inflate(R.layout.assignment_popup_dialog, null);
        assignmentBuilder.setView(assignmentLayout);

        EditText assignmentDate = assignmentLayout.findViewById(R.id.add_assignment_date);
        assignmentDate.setEnabled(false);

        Spinner assignmentClass = assignmentLayout.findViewById(R.id.add_assignment_class_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item,
                ClassElementHandler.getClassNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        assignmentClass.setAdapter(adapter);

        Button datePicker = assignmentLayout.findViewById(R.id.assignment_date_picker);
        datePicker.setOnClickListener(v1 ->
                showDatePickerDialog(listener, context, assignmentDate));

        // add button
        assignmentBuilder.setPositiveButton("OK", (dialog, which) -> {
            EditText assignmentName = assignmentLayout.findViewById(R.id.add_assignment_name);
            TimePicker assignmentTime =  assignmentLayout.findViewById(R.id.assignment_time_picker);
            String assignmentDeadline = getAssignmentDeadlineFromDialog(assignmentTime);

            if (assignmentClass.getSelectedItem() == null) {
                Toast.makeText(context, "Make a class first!",
                        Toast.LENGTH_LONG).show();
                agendaDialog.dismiss();
            }

            if (nonEmptyDialog(assignmentName) && assignmentClass.getSelectedItem() != null) {
                AssignmentElement newAssignment = new AssignmentElement(R.layout.assignment_grid,
                        assignmentName.getText().toString(), assignmentClass.getSelectedItem().toString(),
                        assignmentDeadline, displayMonth, displayDay, displayYear,
                        assignmentTime.getHour(), assignmentTime.getMinute(),
                        assignmentClass.getSelectedItemPosition());

                assignmentAddView(viewGroup, inflater, listener, newAssignment, context);
            }
            agendaDialog.dismiss();
        });
        assignmentBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog assignmentDialog = assignmentBuilder.create();
        assignmentDialog.show();
    }

    /**
     * Adds a new assignment to the LinearLayout of AgendaFragment.
     *
     * @param viewGroup the ViewGroup of AgendaFragment
     * @param inflater the LayoutInflater
     * @param listener the onDateSet listener
     * @param addedAssignment the assignment to be added to the viewGroup
     * @param context the context of the destination fragment
     */
    private void assignmentAddView(ViewGroup viewGroup, LayoutInflater inflater, DatePickerDialog.OnDateSetListener listener, AssignmentElement addedAssignment, Context context) {
        View addedView = inflater.inflate(addedAssignment.getMainResource(), null, false);
        EditText assignmentName = addedView.findViewById(R.id.assignment_title);
        EditText assignmentClass = addedView.findViewById(R.id.assignment_class);
        EditText assignmentDeadline = addedView.findViewById(R.id.assignment_deadline);

        assignmentName.setEnabled(false);
        assignmentName.setText(addedAssignment.getAgendaName());
        assignmentClass.setEnabled(false);
        assignmentClass.setText(addedAssignment.getAgendaClass());
        assignmentDeadline.setEnabled(false);
        assignmentDeadline.setText(addedAssignment.getAgendaDate());

        ImageButton assignmentEditButton = addedView.findViewById(R.id.assignment_edit);
        assignmentEditButton.setOnClickListener(view1 -> assignmentEditDialog(viewGroup,
                inflater, listener, addedView, addedAssignment, context));

        int index = calculateViewPosition(addedAssignment.getAgendaMonth(), addedAssignment.getAgendaDay(), addedAssignment.getAgendaYear(), addedAssignment.getAgendaHour(), addedAssignment.getAgendaMinute());

        AgendaElements.add(index, addedAssignment);

        // resort the linear layout if necessary
        if (sortByClass) agendaSortByClass(viewGroup, inflater, listener, context);
        else viewGroup.addView(addedView, index);
    }

    /**
     * A version of the superclass showEditDialog method, but with extra variables for the
     * different input fields.
     *
     * @see DynamicElementHandler#showEditDialog(String, ViewGroup, LayoutInflater, View, Context, int, int, int)
     */
    private void assignmentEditDialog(ViewGroup viewGroup, LayoutInflater inflater, DatePickerDialog.OnDateSetListener listener,
                                View view, AssignmentElement editedAssignment, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogBoxTheme);
        builder.setTitle("Edit Assignment");

        // set the custom layout
        View customLayout = inflater.inflate(R.layout.assignment_popup_dialog, null);
        builder.setView(customLayout);

        EditText assignmentNameEdit = customLayout.findViewById(R.id.add_assignment_name);
        Spinner assignmentClassEdit = customLayout.findViewById(R.id.add_assignment_class_spinner);
        EditText assignmentDateEdit = customLayout.findViewById(R.id.add_assignment_date);
        TimePicker timePickerEdit = customLayout.findViewById(R.id.assignment_time_picker);

        assignmentDateEdit.setEnabled(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item,
                ClassElementHandler.getClassNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        assignmentClassEdit.setAdapter(adapter);

        Button datePicker = customLayout.findViewById(R.id.assignment_date_picker);
        datePicker.setOnClickListener(v1 ->
                showDatePickerDialog(listener, context, assignmentDateEdit));

        EditText assignmentName = view.findViewById(R.id.assignment_title);
        EditText assignmentClass = view.findViewById(R.id.assignment_class);
        EditText assignmentDate = view.findViewById(R.id.assignment_deadline);

        //set editing window to have same inputs as the selected view
        assignmentNameEdit.setText(editedAssignment.getAgendaName());
        assignmentClassEdit.setSelection(editedAssignment.getClassIndex());
        assignmentDateEdit.setText(editedAssignment.getAgendaDate());

        timePickerEdit.setHour(editedAssignment.getAgendaHour());
        timePickerEdit.setMinute(editedAssignment.getAgendaMinute());

        // add a button
        builder.setPositiveButton("OK", (dialog, which) -> {
            String nameText, dateText, classText;

            if (nonEmptyDialog(assignmentNameEdit)) {
                nameText = assignmentNameEdit.getText().toString();
                if (!editedAssignment.getAgendaDate().equals(assignmentDateEdit.getText().toString())) dateText = getAssignmentDeadlineFromDialog(timePickerEdit);
                else dateText = editedAssignment.getAgendaDate();
                classText = assignmentClassEdit.getSelectedItem().toString();

                assignmentName.setText(nameText);
                assignmentDate.setText(dateText);
                assignmentClass.setText(classText);

                int month = parseDateString(dateText, 0), day = parseDateString(dateText, 1), year = parseDateString(dateText, 2);

                AgendaElements.remove(editedAssignment);

                int index = calculateViewPosition(month, day, year, timePickerEdit.getHour(), timePickerEdit.getMinute());
                AgendaElements.add(index, new AssignmentElement(R.layout.assignment_grid, assignmentName.getText().toString(), assignmentClass.getText().toString(), assignmentDate.getText().toString(), month, day, year, timePickerEdit.getHour(), timePickerEdit.getMinute(), editedAssignment.getClassIndex()));

                if (sortByClass) agendaSortByClass(viewGroup, inflater, listener, context);
                else agendaSortByDate(viewGroup, inflater, listener, context);
            }

        });
        builder.setNeutralButton("Delete", (dialog, which) -> {
            AgendaElements.remove(editedAssignment);
            viewGroup.removeView(view);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Finds the due date and time from a timePicker
     *
     * @param timePicker the timePicker to extract the due date and time from
     * @return a formatted date and time
     */
    private String getAssignmentDeadlineFromDialog(TimePicker timePicker) {
        String time, hour, minute, AMPM;
        hour = (timePicker.getHour() > 12) ?
                String.valueOf(timePicker.getHour() - 12) : String.valueOf(timePicker.getHour());
        minute = (timePicker.getMinute() < 10) ?
                "0" + timePicker.getMinute() : String.valueOf(timePicker.getMinute());
        AMPM = (timePicker.getHour() < 12) ? "AM" : "PM";
        time = hour + ":" + minute + " " + AMPM;

        String date = displayMonth + "/" + displayDay + "/" + displayYear;
        return date + ", " + time;
    }

    /**
     * Shows dialog for adding a new exam.
     *
     * @param inflater the LayoutInflater
     * @param context the context of the destination fragment
     * @param listener the onDateSet listener
     * @param viewGroup the ViewGroup of AgendaFragment
     * @param agendaDialog the dialog of the agendaAddDialog
     */
    private void agendaAddExam(LayoutInflater inflater, Context context, DatePickerDialog.OnDateSetListener listener, ViewGroup viewGroup, AlertDialog agendaDialog) {
        AlertDialog.Builder examBuilder = new AlertDialog.Builder(context, R.style.DialogBoxTheme);
        examBuilder.setTitle("Add Exam");

        View examLayout = inflater.inflate(R.layout.exam_popup_dialog, null);
        examBuilder.setView(examLayout);

        EditText examDateAdd = examLayout.findViewById(R.id.add_exam_date);
        examDateAdd.setEnabled(false);

        Spinner examClass = examLayout.findViewById(R.id.add_exam_class_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item,
                ClassElementHandler.getClassNames());

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        examClass.setAdapter(adapter);

        Button datePicker = examLayout.findViewById(R.id.exam_date_picker);
        datePicker.setOnClickListener(v1 -> showDatePickerDialog(listener, context, examDateAdd));

        // add button
        examBuilder.setPositiveButton("OK", (dialog, which) -> {
            EditText examName = examLayout.findViewById(R.id.add_exam_name);
            TimePicker examTime = examLayout.findViewById(R.id.exam_time_picker);
            EditText examLocation = examLayout.findViewById(R.id.add_exam_location);
            String examDate = getAssignmentDeadlineFromDialog(examTime);

            if (examClass.getSelectedItem() == null) {
                Toast.makeText(context, "Make a class first!",
                        Toast.LENGTH_LONG).show();
                agendaDialog.dismiss();
            }

            if (nonEmptyDialog(examLocation, examName) && examClass.getSelectedItem() != null) {
                ExamElement newExam = new ExamElement(R.layout.exam_grid, examName.getText().toString(),
                        examClass.getSelectedItem().toString(), examDate, displayMonth, displayDay, displayYear,
                        examTime.getHour(), examTime.getMinute(), examClass.getSelectedItemPosition(),
                        examLocation.getText().toString());

                examAddView(viewGroup, inflater, listener, newExam, context);
            }
            agendaDialog.dismiss();
        });
        examBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog examDialog = examBuilder.create();
        examDialog.show();
    }

    /**
     * Adds a new exam to the LinearLayout of AgendaFragment.
     *
     * @param viewGroup the ViewGroup of AgendaFragment
     * @param inflater the LayoutInflater
     * @param listener the onDateSet listener
     * @param addedExam the assignment to be added to the viewGroup
     * @param context the context of the destination fragment
     */
    private void examAddView(ViewGroup viewGroup, LayoutInflater inflater,
                             DatePickerDialog.OnDateSetListener listener, ExamElement addedExam,
                             Context context) {
        View addedView = inflater.inflate(addedExam.getMainResource(), null, false);
        EditText examName = addedView.findViewById(R.id.exam_class);
        EditText examClass = addedView.findViewById(R.id.exam_title);
        EditText examDate = addedView.findViewById(R.id.exam_time);
        EditText examLocation = addedView.findViewById(R.id.exam_location);

        examName.setEnabled(false);
        examName.setText(addedExam.getAgendaName());
        examClass.setEnabled(false);
        examClass.setText(addedExam.getAgendaClass());
        examDate.setEnabled(false);
        examDate.setText(addedExam.getAgendaDate());
        examLocation.setEnabled(false);
        examLocation.setText(addedExam.getLocation());

        ImageButton examEditButton = addedView.findViewById(R.id.exam_edit);
        examEditButton.setOnClickListener(view1 -> examEditDialog(viewGroup,
                inflater, listener, addedView, addedExam, context));

        int index = calculateViewPosition(addedExam.getAgendaMonth(), addedExam.getAgendaDay(), addedExam.getAgendaYear(), addedExam.getAgendaHour(), addedExam.getAgendaMinute());

        AgendaElements.add(index, addedExam);

        if (sortByClass) agendaSortByClass(viewGroup, inflater, listener, context);
        else viewGroup.addView(addedView, index);
    }

    /**
     * A version of the superclass showEditDialog method, but with extra variables for the
     * different input fields.
     *
     * @see DynamicElementHandler#showEditDialog(String, ViewGroup, LayoutInflater, View, Context, int, int, int)
     */
    private void examEditDialog(ViewGroup viewGroup, LayoutInflater inflater, DatePickerDialog.OnDateSetListener listener,
                                View view, ExamElement editedExam, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogBoxTheme);
        builder.setTitle("Edit Exam");

        // set the custom layout
        View customLayout = inflater.inflate(R.layout.exam_popup_dialog, null);
        builder.setView(customLayout);

        EditText examNameEdit = customLayout.findViewById(R.id.add_exam_name);
        Spinner examClassEdit = customLayout.findViewById(R.id.add_exam_class_spinner);
        EditText examDateEdit = customLayout.findViewById(R.id.add_exam_date);
        TimePicker timePickerEdit = customLayout.findViewById(R.id.exam_time_picker);
        EditText examLocationEdit = customLayout.findViewById(R.id.add_exam_location);

        examDateEdit.setEnabled(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item,
                ClassElementHandler.getClassNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        examClassEdit.setAdapter(adapter);

        Button datePicker = customLayout.findViewById(R.id.exam_date_picker);
        datePicker.setOnClickListener(v1 ->
                showDatePickerDialog(listener, context, examDateEdit));

        EditText examName = view.findViewById(R.id.exam_title);
        EditText examClass = view.findViewById(R.id.exam_class);
        EditText examDate = view.findViewById(R.id.exam_time);
        EditText examLocation = view.findViewById(R.id.exam_location);

        //set editing window to have same inputs as the selected view
        examNameEdit.setText(editedExam.getAgendaName());
        examClassEdit.setSelection(editedExam.getClassIndex());
        examDateEdit.setText(editedExam.getAgendaDate());
        examLocationEdit.setText(editedExam.getLocation());

        timePickerEdit.setHour(editedExam.getAgendaHour());
        timePickerEdit.setMinute(editedExam.getAgendaMinute());
        // add a button
        builder.setPositiveButton("OK", (dialog, which) -> {
            String nameText, dateText, classText, locationText;

            if (nonEmptyDialog(examNameEdit)) {
                nameText = examNameEdit.getText().toString();
                if (!editedExam.getAgendaDate().equals(examDateEdit.getText().toString())) dateText = getAssignmentDeadlineFromDialog(timePickerEdit);
                else dateText = editedExam.getAgendaDate();
                classText = examClassEdit.getSelectedItem().toString();
                locationText = examLocationEdit.getText().toString();

                examName.setText(nameText);
                examDate.setText(dateText);
                examClass.setText(classText);
                examLocation.setText(locationText);

                int month = parseDateString(dateText, 0), day = parseDateString(dateText, 1), year = parseDateString(dateText, 2);

                AgendaElements.remove(editedExam);

                int index = calculateViewPosition(month, day, year, timePickerEdit.getHour(), timePickerEdit.getMinute());
                AgendaElements.add(index, new ExamElement(R.layout.exam_grid, examName.getText().toString(),
                        examClass.getText().toString(), examDate.getText().toString(), month, day, year,
                        timePickerEdit.getHour(), timePickerEdit.getMinute(), editedExam.getClassIndex(),
                        examLocation.getText().toString()));

                if (sortByClass) agendaSortByClass(viewGroup, inflater, listener, context);
                else agendaSortByDate(viewGroup, inflater, listener, context);
            }

        });
        builder.setNeutralButton("Delete", (dialog, which) -> {
            AgendaElements.remove(editedExam);
            viewGroup.removeView(view);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * Displays dialog for calendar popup on "Pick Date" button click.
     *
     * @param listener the onDateSet listener
     * @param context the context of the destination fragment
     */
    private void showDatePickerDialog(DatePickerDialog.OnDateSetListener listener, Context context,
                                      EditText editedDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context, listener,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        DatePicker picker = datePickerDialog.getDatePicker();
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Set Date", (dialog, which) -> {
            listener.onDateSet(picker, picker.getYear(),
                    picker.getMonth(), picker.getDayOfMonth());
            String date = displayMonth + "/" + displayDay + "/" + displayYear;
            editedDate.setText(date);
        });
        datePickerDialog.show();
    }

    /**
     * Sets the class variables for the date.
     *
     * @param year the year that was selected
     * @param month the month that was selected
     * @param day the day that was selected
     */
    public void agendaSetDate(int year, int month, int day) {
        displayDay = day;
        displayMonth = month;
        displayYear = year;
    }

    /**
     * Sorts the viewGroup by class name.
     *
     * @param viewGroup the ViewGroup of AgendaFragment
     * @param inflater the LayoutInflater
     * @param listener the onDateSet listener
     * @param context the context of the destination fragment
     */
    public void agendaSortByClass(ViewGroup viewGroup, LayoutInflater inflater, DatePickerDialog.OnDateSetListener listener, Context context) {
        sortByClass = true;
        ArrayList<AgendaElement> AgendaElementClassSorted = new ArrayList<>(AgendaElements);

        Collections.sort(AgendaElementClassSorted, AgendaElement.dateSort);

        if (showElements == 0) showAll(viewGroup, inflater, AgendaElementClassSorted, listener, context);
        else if (showElements == 1) showAssignmentsOrExams(viewGroup, inflater, true, AgendaElementClassSorted, listener, context);
        else showAssignmentsOrExams(viewGroup, inflater, false, AgendaElementClassSorted, listener, context);
    }

    /**
     * Sorts the viewGroup by date.
     *
     * @param viewGroup the ViewGroup of AgendaFragment
     * @param inflater the LayoutInflater
     * @param listener the onDateSet listener
     * @param context the context of the destination fragment
     */
    public void agendaSortByDate(ViewGroup viewGroup, LayoutInflater inflater, DatePickerDialog.OnDateSetListener listener, Context context) {
        sortByClass = false;
        if (showElements == 0) showAll(viewGroup, inflater, AgendaElements, listener, context);
        else if (showElements == 1) showAssignmentsOrExams(viewGroup, inflater, true, AgendaElements, listener, context);
        else showAssignmentsOrExams(viewGroup, inflater, false, AgendaElements, listener, context);
    }

    /**
     * Shows both assignments and exams in the viewGroup.
     *
     * @param viewGroup the ViewGroup of AgendaFragment
     * @param inflater the LayoutInflater
     * @param filterList an ArrayList which contains the sort order of AgendaElements
     * @param listener the onDateSet listener
     * @param context the context of the destination fragment
     */
    public void showAll(ViewGroup viewGroup, LayoutInflater inflater, ArrayList<AgendaElement> filterList, DatePickerDialog.OnDateSetListener listener, Context context) {
        showElements = 0;

        viewGroup.removeAllViews();
        for (AgendaElement i:filterList) {
            View newView = inflater.inflate(i.getMainResource(), null, false);

            if (i instanceof AssignmentElement) {
                EditText assignmentName = newView.findViewById(R.id.assignment_title);
                EditText assignmentClass = newView.findViewById(R.id.assignment_class);
                EditText assignmentDeadline = newView.findViewById(R.id.assignment_deadline);

                assignmentName.setEnabled(false);
                assignmentName.setText(i.getAgendaName());
                assignmentClass.setEnabled(false);
                assignmentClass.setText(i.getAgendaClass());
                assignmentDeadline.setEnabled(false);
                assignmentDeadline.setText(i.getAgendaDate());

                ImageButton assignmentEditButton = newView.findViewById(R.id.assignment_edit);
                assignmentEditButton.setOnClickListener(view1 -> assignmentEditDialog(viewGroup,
                        inflater, listener, newView, (AssignmentElement) i, context));
            } else if (i instanceof ExamElement) {
                EditText examName = newView.findViewById(R.id.exam_class);
                EditText examClass = newView.findViewById(R.id.exam_title);
                EditText examDate = newView.findViewById(R.id.exam_time);
                EditText examLocation = newView.findViewById(R.id.exam_location);

                examName.setEnabled(false);
                examName.setText(i.getAgendaName());
                examClass.setEnabled(false);
                examClass.setText(i.getAgendaClass());
                examDate.setEnabled(false);
                examDate.setText(i.getAgendaDate());
                examLocation.setEnabled(false);
                examLocation.setText(((ExamElement) i).getLocation());

                ImageButton examEditButton = newView.findViewById(R.id.exam_edit);
                examEditButton.setOnClickListener(view1 -> examEditDialog(viewGroup,
                        inflater, listener, newView, (ExamElement) i, context));
            }
            viewGroup.addView(newView);
        }
    }

    /**
     * Shows either assignments or exams in the viewGroup.
     *
     * @param viewGroup the ViewGroup of AgendaFragment
     * @param inflater the LayoutInflater
     * @param showAssignment boolean which decides whether to show assignments or exams
     * @param filterList an ArrayList which contains the sort order of AgendaElements
     * @param listener the onDateSet listener
     * @param context the context of the destination fragment
     */
    public void showAssignmentsOrExams(ViewGroup viewGroup, LayoutInflater inflater, boolean showAssignment, ArrayList<AgendaElement> filterList, DatePickerDialog.OnDateSetListener listener, Context context) {
        if (showAssignment) showElements = 1;
        else showElements = 2;

        viewGroup.removeAllViews();
        for (AgendaElement i:filterList) {
            View newView = inflater.inflate(i.getMainResource(), null, false);
            if (showAssignment) {
                if (i instanceof AssignmentElement) {
                    EditText assignmentName = newView.findViewById(R.id.assignment_title);
                    EditText assignmentClass = newView.findViewById(R.id.assignment_class);
                    EditText assignmentDeadline = newView.findViewById(R.id.assignment_deadline);

                    assignmentName.setEnabled(false);
                    assignmentName.setText(i.getAgendaName());
                    assignmentClass.setEnabled(false);
                    assignmentClass.setText(i.getAgendaClass());
                    assignmentDeadline.setEnabled(false);
                    assignmentDeadline.setText(i.getAgendaDate());

                    ImageButton assignmentEditButton = newView.findViewById(R.id.assignment_edit);
                    assignmentEditButton.setOnClickListener(view1 -> assignmentEditDialog(viewGroup,
                            inflater, listener, newView, (AssignmentElement) i, context));

                    viewGroup.addView(newView);
                }
            } else {
                if (i instanceof ExamElement) {
                    EditText examName = newView.findViewById(R.id.exam_class);
                    EditText examClass = newView.findViewById(R.id.exam_title);
                    EditText examDate = newView.findViewById(R.id.exam_time);
                    EditText examLocation = newView.findViewById(R.id.exam_location);

                    examName.setEnabled(false);
                    examName.setText(i.getAgendaName());
                    examClass.setEnabled(false);
                    examClass.setText(i.getAgendaClass());
                    examDate.setEnabled(false);
                    examDate.setText(i.getAgendaDate());
                    examLocation.setEnabled(false);
                    examLocation.setText(((ExamElement) i).getLocation());

                    ImageButton examEditButton = newView.findViewById(R.id.exam_edit);
                    examEditButton.setOnClickListener(view1 -> examEditDialog(viewGroup,
                            inflater, listener, newView, (ExamElement) i, context));

                    viewGroup.addView(newView);
                }
            }
        }
    }

    /**
     * Finds the index at which a given view belongs by date ordering
     *
     * @param month the month of the view
     * @param day the day of the view
     * @param year the year of the view
     * @param hour the hour of the view
     * @param minute the minute of the view
     * @return the index at which the view belongs when sorted by date
     */
    private int calculateViewPosition(int month, int day, int year, int hour, int minute) {
        int index = 0;

        while (index < AgendaElements.size() && year > AgendaElements.get(index).getAgendaYear()) index++;
        while (index < AgendaElements.size() && year == AgendaElements.get(index).getAgendaYear() && month > AgendaElements.get(index).getAgendaMonth()) index++;
        while (index < AgendaElements.size() && year == AgendaElements.get(index).getAgendaYear() && month == AgendaElements.get(index).getAgendaMonth() &&
                day > AgendaElements.get(index).getAgendaDay()) index++;
        while (index < AgendaElements.size() && year == AgendaElements.get(index).getAgendaYear() && month == AgendaElements.get(index).getAgendaMonth() &&
                day == AgendaElements.get(index).getAgendaDay() && hour > AgendaElements.get(index).getAgendaHour()) index++;
        while (index < AgendaElements.size() && year == AgendaElements.get(index).getAgendaYear() && month == AgendaElements.get(index).getAgendaMonth() &&
                day == AgendaElements.get(index).getAgendaDay() && hour == AgendaElements.get(index).getAgendaHour() && minute > AgendaElements.get(index).getAgendaMinute()) index++;

        return index;
    }

    /**
     * Parses a string date into an integer for either the month, day, or year.
     *
     * @param date the date to parse
     * @param which determines which time unit to return
     * @return either the month, day, or year as an int
     */
    private int parseDateString(String date, int which) {
        String[] dates = date.split("/");
        if (which == 0) return Integer.parseInt(dates[0]);
        else if (which == 1) return Integer.parseInt(dates[1]);
        else return Integer.parseInt(dates[2].substring(0,4));
    }

    public ArrayList<AgendaElement> getAgendaElements() {
        return AgendaElements;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public void setInflater(LayoutInflater inflater) {
        this.inflater = inflater;
    }
}
