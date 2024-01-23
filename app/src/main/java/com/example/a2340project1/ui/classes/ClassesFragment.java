package com.example.a2340project1.ui.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.a2340project1.R;
import com.example.a2340project1.databinding.FragmentClassesBinding;
import com.google.android.material.snackbar.Snackbar;

public class ClassesFragment extends Fragment {

    private FragmentClassesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ClassesViewModel classesViewModel =
                new ViewModelProvider(this).get(ClassesViewModel.class);

        binding = FragmentClassesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textClasses;
        classesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.add_classes_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                // edit the snackbar later ofc
                // create a grid from this in which the grid has a layout for all of the stuff in the class
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}