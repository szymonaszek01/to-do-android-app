package com.app.toDo.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.toDo.R;
import com.app.toDo.adapters.RvAdapter;
import com.app.toDo.configuration.DatabaseConfiguration;
import com.app.toDo.databinding.FragmentTasksBinding;
import com.app.toDo.entity.Category;
import com.app.toDo.entity.Notification;
import com.app.toDo.entity.Task;
import com.app.toDo.model.AppViewModel;
import com.app.toDo.notification.TaskNotificationManager;
import com.app.toDo.service.CategoryService;
import com.app.toDo.service.NotificationService;
import com.app.toDo.service.TaskService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TasksFragment extends Fragment {

    private final String[] items = {"60 minutes", "30 minutes", "15 minutes"};
    int checkedDialogFilterItem = -1;
    int checkedDialogNotificationItem = 0;
    private TaskService taskService;
    private CategoryService categoryService;
    private NotificationService notificationService;
    private TaskNotificationManager taskNotificationManager;
    private String filteringCategory;
    private String notificationTime;
    private FragmentTasksBinding binding;
    private AppViewModel appViewModel;
    private RvAdapter adapter;

    public TasksFragment() {
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

        binding = FragmentTasksBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        checkedDialogNotificationItem = getNotificationIndexOnInit();
        notificationTime = items[checkedDialogNotificationItem];

        binding.toolbar.inflateMenu(R.menu.toolbar_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.filter_action:
                    setupFilterDialog();
                    return true;
                case R.id.hide_outdated:
                    hideOutdatedTasks();
                    return true;
                case R.id.show_outdated:
                    showOutdatedTasks();
                    return true;
                case R.id.select_notification_time:
                    setupNotificationDialog();
                    return true;
                default:
                    return false;
            }
        });

        List<Task> taskList = sortByExecDateTimeEpochDescending(taskService.getTaskList());
        List<Category> categoryList = categoryService.getCategoryList();
        appViewModel.setCategoryList(categoryList);
        appViewModel.setTaskList(taskList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.tasksRv.setLayoutManager(layoutManager);
        adapter = new RvAdapter(taskList);
        binding.tasksRv.setAdapter(adapter);

        addTaskBtnInit(root);
        findTaskBtnInit(root);
        observeAppViewModel();

        return root;
    }

    private void addTaskBtnInit(View root) {
        binding.addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_tasksFragment_to_addTaskFragment);
            }
        });
    }

    private void findTaskBtnInit(View root) {
        binding.findTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Task> taskList = taskService.getTaskList()
                        .stream()
                        .filter(task -> task.getTitle().contains(s.toString()))
                        .collect(Collectors.toList());
                appViewModel.setTaskList(taskList);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void observeAppViewModel() {
        Log.i("VM", "data changed");
        appViewModel.getTaskList().observe(getViewLifecycleOwner(), tasks -> {
            adapter.setNewTasks(sortByExecDateTimeEpochDescending(tasks));
            binding.tasksRv.setAdapter(adapter);
        });
    }

    private void setupFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Choose filters");
        List<Category> categoryList = appViewModel.getCategoryList().getValue();
        String[] items = categoryList.stream().map(Category::toString).collect(Collectors.toList()).toArray(new String[0]);
        builder.setSingleChoiceItems(items, checkedDialogFilterItem, (dialog, which) -> {
            checkedDialogFilterItem = which;
            filteringCategory = items[which];
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            List<Task> taskList = taskService.getTaskList();
            appViewModel.setTaskList(categoryService.filterTaskListByCategoryName(taskList, filteringCategory));
            appViewModel.setFilteringCategory(filteringCategory);
        });

        builder.setNegativeButton("Clear", (dialog, which) -> {
            checkedDialogFilterItem = -1;
            appViewModel.setTaskList(taskService.getTaskList());
            appViewModel.setFilteringCategory("");
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setupNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Choose notification time");

        builder.setSingleChoiceItems(items, checkedDialogNotificationItem, (dialog, which) -> {
            checkedDialogNotificationItem = which;
            notificationTime = items[which];
        });

        ZoneOffset zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now());
        builder.setPositiveButton("OK", (dialog, which) -> {
            appViewModel.setNotificationTimeInSeconds(getNotificationTimeAsInt());
            List<Notification> notificationList = notificationService.getNotificationList()
                    .stream()
                    .filter(notification -> notification.getExecDateTimeEpoch() > LocalDateTime.now().toEpochSecond(zoneOffset))
                    .collect(Collectors.toList());

            notificationList.forEach(notification -> {
                notification.setNotificationDateTimeEpoch(notification.getExecDateTimeEpoch() - getNotificationTimeAsInt());
                notificationService.editNotification(notification);
                taskNotificationManager.cancelTaskNotification(notification);
                taskNotificationManager.scheduleTaskNotification(notification);
            });
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void hideOutdatedTasks() {
        ZoneOffset zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now());
        List<Task> taskList = appViewModel.getTaskList().getValue()
                .stream()
                .filter(task -> task.getExecDateTimeEpoch() > LocalDateTime.now().toEpochSecond(zoneOffset))
                .collect(Collectors.toList());
        appViewModel.setTaskList(sortByExecDateTimeEpochDescending(taskList));
    }

    private void showOutdatedTasks() {
        List<Task> taskList = taskService.getTaskList();
        String filter = appViewModel.getFilteringCategory().getValue();
        appViewModel.setTaskList(sortByExecDateTimeEpochDescending(categoryService.filterTaskListByCategoryName(taskList, filter)));
    }

    private List<Task> sortByExecDateTimeEpochDescending(List<Task> taskList) {
        taskList.sort(new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                ZoneOffset zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now());
                long currentTime = LocalDateTime.now().toEpochSecond(zoneOffset);
                long timeDifference1 = Math.abs(currentTime - task1.getExecDateTimeEpoch());
                long timeDifference2 = Math.abs(currentTime - task2.getExecDateTimeEpoch());

                if (currentTime > task1.getExecDateTimeEpoch() && currentTime > task2.getExecDateTimeEpoch()) {
                    return 0; // Both tasks are outdated, consider them equal
                } else if (currentTime > task1.getExecDateTimeEpoch()) {
                    return 1; // Only task1 is outdated, place it after task2
                } else if (currentTime > task2.getExecDateTimeEpoch()) {
                    return -1; // Only task2 is outdated, place it after task1
                } else {
                    return Long.compare(timeDifference1, timeDifference2);
                }
            }
        });

        return taskList;
    }

    private int getNotificationTimeAsInt() {
        if ("60 minutes".equals(notificationTime)) {
            return 60 * 60;
        } else if ("30 minutes".equals(notificationTime)) {
            return 30 * 60;
        } else {
            return 15 * 60;
        }
    }

    private int getNotificationIndexOnInit() {
        if (appViewModel.getNotificationTimeInSeconds().getValue() == 60 * 60) {
            return 0;
        } else if (appViewModel.getNotificationTimeInSeconds().getValue() == 30 * 60) {
            return 1;
        } else {
            return 2;
        }
    }
}