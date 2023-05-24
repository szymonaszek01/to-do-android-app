package com.app.toDo.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.app.toDo.R;
import com.app.toDo.configuration.DatabaseConfiguration;
import com.app.toDo.databinding.FragmentEditTaskBinding;
import com.app.toDo.entity.Task;
import com.app.toDo.model.AppViewModel;
import com.app.toDo.service.CategoryService;
import com.app.toDo.service.TaskService;
import com.app.toDo.util.DateConverter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class EditTaskFragment extends Fragment {

    private final ZoneOffset zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now());

    int selectedCategoryIndex = 0;

    private TaskService taskService;

    private CategoryService categoryService;

    private Task task;

    private FragmentEditTaskBinding binding;

    private AppViewModel appViewModel;

    public EditTaskFragment() {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        task.setTitle(binding.taskNameView.getText().toString());
        task.setDesc(binding.taskDescView.getText().toString());
        if (binding.categorySelectView.getListSelection() != ListView.INVALID_POSITION) {
            task.setCategoryId(binding.categorySelectView.getListSelection() + 1);
        }
        appViewModel.setSelectedTask(task);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        categoryService = CategoryService.builder().categoryDao(DatabaseConfiguration.getInstance(getContext()).categoryDao()).build();
        taskService = TaskService.builder().taskDao(DatabaseConfiguration.getInstance(getContext()).taskDao()).build();
        binding = FragmentEditTaskBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        long taskId = EditTaskFragmentArgs.fromBundle(getArguments()).getTaskIdToEdit();
        task = appViewModel.getDefaultTask();
        if (taskService.getTaskById(taskId).isPresent()) {
            task = taskService.getTaskById(taskId).get();
        }
        updateTaskFieldList();

        binding.categorySelectView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategoryIndex = i;
            }
        });

        binding.link.setOnClickListener(x -> {
            try {
                openImage(Uri.parse(task.getUri()));
            } catch (ActivityNotFoundException | IOException e) {
                e.printStackTrace();
            }
        });

        binding.addNotifyCheckbox.setOnClickListener(x -> {
            task.setNotificationOn(binding.addNotifyCheckbox.isChecked());
        });

        observeAppViewModel();
        addCategoryBtnInit();
        dateTimePickInit();
        updateTaskBtnInit();
        return root;
    }

    private void addCategoryBtnInit() {
        binding.createCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTask();
                appViewModel.setSelectedTask(task);
                Navigation.findNavController(view).navigate(R.id.action_editTaskFragment_to_addCategoryFragment);
            }
        });
    }


    private void updateTask() {
        task.setTitle(binding.taskNameView.getText().toString());
        task.setDesc(binding.taskDescView.getText().toString());
        if (selectedCategoryIndex != -1) {
            if (selectedCategoryIndex != 0) {
                task.setCategoryId(appViewModel.getCategoryList().getValue().get(selectedCategoryIndex).getId());
            }
        }
        task.setUri(binding.link.getText().toString());
        task.setNotificationOn(binding.addNotifyCheckbox.isChecked());
    }

    private void updateTaskFieldList() {
        binding.taskNameView.setText(task.getTitle());
        binding.taskDescView.setText(task.getDesc());
        binding.categorySelectView.setListSelection((int) task.getCategoryId());
        binding.datePickerView.setText(DateConverter.getPrettyLocalDateTime(task.getExecDateTimeEpoch()));
        binding.createdAt.setText(DateConverter.getPrettyLocalDateTime(task.getCreationDateTimeEpoch()));
        binding.link.setText(task.getUri());
        binding.addNotifyCheckbox.setChecked(task.isNotificationOn());
        binding.categorySelectView.setText(categoryService.getCategoryNameById(task.getCategoryId()));

        binding.categorySelectView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategoryIndex = i;
            }
        });
    }

    private void observeAppViewModel() {
        appViewModel.getCategoryList().observe(getViewLifecycleOwner(), categoryList -> {
            ArrayAdapter adapter = new ArrayAdapter(requireContext(), R.layout.category_dropdown_list_item, categoryList);
            binding.categorySelectView.setAdapter(adapter);
            binding.categorySelectView.setText(categoryService.getCategoryNameById(task.getCategoryId()), false);
        });
    }

    private void dateTimePickInit() {
        binding.datePickerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTask();
                MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();
                picker.show(requireActivity().getSupportFragmentManager(), "date_pick");

                //select time
                MaterialTimePicker timePicker =
                        new MaterialTimePicker.Builder()
                                .setTimeFormat(TimeFormat.CLOCK_24H)
                                .setHour(12)
                                .setMinute(00)
                                .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                                .build();
                timePicker.show(requireActivity().getSupportFragmentManager(), "tag");
                picker.addOnPositiveButtonClickListener(x -> {
                    LocalDate selectedDate = Instant.ofEpochMilli(Long.parseLong(picker.getSelection().toString())).atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDateTime combinedDateAndTime = LocalDateTime.of(selectedDate, LocalTime.of(timePicker.getHour(), timePicker.getMinute()));
                    Log.i("time", combinedDateAndTime.toString());
                    task.setCreationDateTimeEpoch(combinedDateAndTime.toEpochSecond(zoneOffset));
                    task.setExecDateTimeEpoch(combinedDateAndTime.toEpochSecond(zoneOffset));
                    updateTaskFieldList();
                });
                updateTaskFieldList();
            }
        });
    }

    private void updateTaskBtnInit() {
        binding.createTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFields()) {
                    return;
                }
                updateTask();

                taskService.editTask(task);
                appViewModel.setSelectedTask(appViewModel.getDefaultTask());
                Navigation.findNavController(view).navigate(R.id.action_editTaskFragment_to_tasksFragment);
            }
        });
    }


    private void openImage(Uri uri) throws IOException {
        Intent openImage = new Intent(Intent.ACTION_VIEW, uri);
        openImage.setDataAndType(uri, "image/*");
        startActivity(openImage);
    }

    private boolean validateFields() {
        boolean isOk = true;
        if (binding.taskNameView.getText().length() == 0) {
            isOk = false;
            binding.taskNameView.setError("Can't be empty!");
        }
        if (binding.taskDescView.getText().length() == 0) {
            isOk = false;
            binding.taskDescView.setError("Can't be empty!");
        }
        if (selectedCategoryIndex == -1) {
            Toast.makeText(requireActivity(), "Select category or add new if it's your first!", Toast.LENGTH_LONG).show();
            isOk = false;
            binding.categorySelectView.setError("Select one!");
        }
        if (DateConverter.getPrettyLocalDateTime(task.getExecDateTimeEpoch()) == null) {
            isOk = false;
            binding.datePickerView.setError("Select date and time!");
        }
        return isOk;
    }
}
