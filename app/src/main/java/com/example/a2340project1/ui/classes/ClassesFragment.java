package com.example.a2340project1.ui.classes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.a2340project1.R;
import com.example.a2340project1.databinding.FragmentClassesBinding;
import com.example.a2340project1.ui.DynamicElementHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ClassesFragment extends Fragment {

    private FragmentClassesBinding binding;

    private LinearLayout layoutList;
    private static final ClassElementHandler ELEMENT_HANDLER = new ClassElementHandler();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ClassesViewModel classesViewModel =
                new ViewModelProvider(this).get(ClassesViewModel.class);

        binding = FragmentClassesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        layoutList = root.findViewById(R.id.classes_layout);

        //deal with this later
        int temp = ELEMENT_HANDLER.getViewCount();
        ELEMENT_HANDLER.setViewCount(0);
        for (int i = 0; i < temp; i++) {
            ClassElement newClass = new ClassElement(R.layout.class_grid, R.id.delete_class,
                    "", "", "");
            ELEMENT_HANDLER.addView(layoutList, getLayoutInflater(), newClass);
        }

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button classAddButton = view.findViewById(R.id.add_classes_button);
        classAddButton.setOnClickListener(view1 -> ELEMENT_HANDLER.classAddDialog(layoutList,
                getLayoutInflater(), this.getContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}