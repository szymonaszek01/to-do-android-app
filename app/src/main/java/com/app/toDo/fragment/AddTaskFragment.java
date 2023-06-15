package com.app.toDo.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.app.toDo.R;
import com.app.toDo.configuration.DatabaseConfiguration;
import com.app.toDo.databinding.FragmentAddTaskBinding;
import com.app.toDo.entity.Notification;
import com.app.toDo.entity.Task;
import com.app.toDo.model.AppViewModel;
import com.app.toDo.notification.TaskNotificationManager;
import com.app.toDo.service.CategoryService;
import com.app.toDo.service.NotificationService;
import com.app.toDo.service.TaskService;
import com.app.toDo.util.DateConverter;
import com.app.toDo.util.FileCopyClass;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.FileDescriptor;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class AddTaskFragment extends Fragment {

    private CategoryService categoryService;

    private TaskService taskService;

    private NotificationService notificationService;

    private TaskNotificationManager taskNotificationManager;

    private final ZoneOffset zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now());

    private int selectedCategoryIndex = -1;

    private Uri imageUri;

    private FragmentAddTaskBinding binding;

    private AppViewModel appViewModel;

    private Bitmap loadedImage;

    private Task task;

    public AddTaskFragment() {}

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        task.setTitle(binding.taskNameView.getText().toString());
        task.setDesc(binding.taskDescView.getText().toString());
        if (binding.categorySelectView.getListSelection() != ListView.INVALID_POSITION && selectedCategoryIndex != -1) {
            task.setCategoryId(binding.categorySelectView.getListSelection());
        }
        appViewModel.setSelectedTask(task);

        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        categoryService = CategoryService.builder().categoryDao(DatabaseConfiguration.getInstance(getContext()).categoryDao()).build();
        taskService = TaskService.builder().taskDao(DatabaseConfiguration.getInstance(getContext()).taskDao()).build();
        notificationService = NotificationService.builder().notificationDao(DatabaseConfiguration.getInstance(getContext()).notificationDao()).build();
        taskNotificationManager = TaskNotificationManager.builder().context(getContext()).build();

        binding = FragmentAddTaskBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        task = appViewModel.getSelectedTask().getValue();
        updateTaskFieldList();

        getAttachmentURI();
        createTaskBtnInit();
        dateTimePickInit();

        addCategoryBtnInit();
        observeAppViewModel();

        return root;
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void observeAppViewModel() {
        appViewModel.getCategoryList().observe(getViewLifecycleOwner(), categoryList -> {
            ArrayAdapter adapter = new ArrayAdapter(requireContext(), R.layout.category_dropdown_list_item, categoryList);
            binding.categorySelectView.setAdapter(adapter);
        });
    }

    private void createTaskBtnInit() {
        binding.createTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFields()) {
                    return;
                }
                updateTask();
                if (task.isNotificationOn()) {
                    Notification notification = createNotification();
                    notificationService.addNotification(notification);
                    task.setNotificationCounter(notification.getCounter());
                    taskNotificationManager.scheduleTaskNotification(notification);
                }

                task.setUri(FileCopyClass.builder().context(getContext()).build().copyAttachmentToExternal(imageUri).toString());
                taskService.addTask(task);
                appViewModel.setSelectedTask(appViewModel.getDefaultTask());
                Navigation.findNavController(view).navigate(R.id.action_addTaskFragment_to_tasksFragment);
            }
        });
    }

    private Notification createNotification() {
        return Notification.builder()
                .id(0L)
                .counter(notificationService.countAll() + 1)
                .title(task.getTitle())
                .message(task.getDesc())
                .execDateTimeEpoch(task.getExecDateTimeEpoch())
                .notificationDateTimeEpoch(task.getExecDateTimeEpoch() - appViewModel.getNotificationTimeInSeconds().getValue())
                .build();
    }

    private void updateTask() {
        task.setTitle(binding.taskNameView.getText().toString());
        task.setDesc(binding.taskDescView.getText().toString());
        if (selectedCategoryIndex != -1) {
            task.setCategoryId(appViewModel.getCategoryList().getValue().get(selectedCategoryIndex).getId());
        }
        task.setUri(binding.link.getText().toString());
        task.setNotificationOn(binding.addNotifyCheckbox.isChecked());
    }

    private void updateTaskFieldList() {
        binding.taskNameView.setText(task.getTitle());
        binding.taskDescView.setText(task.getDesc());
        binding.categorySelectView.setListSelection((int) task.getCategoryId());
        binding.datePickerView.setText(DateConverter.getPrettyLocalDateTime(task.getExecDateTimeEpoch()));
        binding.link.setText(task.getUri());
        imageUri = new Uri.Builder().path(task.getUri()).build();
        binding.addNotifyCheckbox.setChecked(task.isNotificationOn());
        binding.categorySelectView.setText(categoryService.getCategoryNameById(task.getCategoryId()));

        binding.categorySelectView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategoryIndex = i;
            }
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
                    binding.datePickerView.setText(DateConverter.getPrettyLocalDateTime(combinedDateAndTime.toEpochSecond(zoneOffset)));
                    task.setExecDateTimeEpoch(combinedDateAndTime.toEpochSecond(zoneOffset));
                    updateTaskFieldList();
                });
                updateTaskFieldList();
            }
        });
    }

    private void addCategoryBtnInit() {
        binding.createCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTask();
                appViewModel.setSelectedTask(task);
                Navigation.findNavController(view).navigate(R.id.action_addTaskFragment_to_addCategoryFragment);
            }
        });
    }

    private void getAttachmentURI() {
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        try {
                            Log.i("open", uri.getPath());
                            loadedImage = getBitmapFromUri(uri);
                            imageUri = uri;
                            binding.link.setText(uri.toString());
                        } catch (NullPointerException | IOException e) {
                            imageUri = Uri.EMPTY;
                        }
                    }
                });

        binding.link.setOnClickListener(x -> {
            try {
                openImage(imageUri);
            } catch (ActivityNotFoundException | IOException e) {
                e.printStackTrace();
            }
        });

        binding.attachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
                binding.imageView.setImageBitmap(loadedImage);
            }
        });
    }

    private void openImage(Uri uri) throws IOException {
        Intent openImage = new Intent(Intent.ACTION_VIEW, uri);
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