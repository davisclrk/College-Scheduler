package com.example.a2340project1.ui.agenda;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.a2340project1.R;
import com.example.a2340project1.databinding.FragmentAgendaBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class AgendaFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private FragmentAgendaBinding binding;
    private final AgendaElementHandler ELEMENT_HANDLER = new AgendaElementHandler();

    private LinearLayout layoutList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AgendaViewModel agendaViewModel =
                new ViewModelProvider(this).get(AgendaViewModel.class);

        binding = FragmentAgendaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        layoutList = root.findViewById(R.id.agenda_layout);

        return root;
    }

    // NOTE: there should be 2 filters at the top, one for showing exam/assignment/both and one for sorting by date/class
    // ngl idk how to do the exam/assignment/both filter

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton agendaAddButton = view.findViewById(R.id.add_agenda_button);
        agendaAddButton.setOnClickListener(v -> {
            ELEMENT_HANDLER.agendaAddDialog(layoutList, getLayoutInflater(), this.getContext(),
                    this);
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        ELEMENT_HANDLER.agendaSetDate(year, month+1, dayOfMonth);
    }
}