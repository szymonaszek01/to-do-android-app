package com.app.toDo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.toDo.R;
import com.app.toDo.adapters.RvAdapter;
import com.app.toDo.configuration.DatabaseConfiguration;
import com.app.toDo.databinding.FragmentTasksBinding;
import com.app.toDo.entity.Category;
import com.app.toDo.entity.Task;
import com.app.toDo.model.AppViewModel;
import com.app.toDo.service.CategoryService;
import com.app.toDo.service.TaskService;

import java.util.List;

public class TasksFragment extends Fragment {

    int checkedDialogItem = -1;

    private TaskService taskService;

    private CategoryService categoryService;

    private String filteringCategory;

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


        binding = FragmentTasksBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        List<Task> taskList = taskService.getTaskList();
        List<Category> categoryList = categoryService.getCategoryList();
        appViewModel.setCategoryList(categoryList);
        appViewModel.setTaskList(taskService.getTaskList());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.tasksRv.setLayoutManager(layoutManager);
        adapter = new RvAdapter(taskList);
        binding.tasksRv.setAdapter(adapter);

        addTaskBtnInit(root);
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

    private void observeAppViewModel() {
        Log.i("VM", "data changed");
        appViewModel.getTaskList().observe(getViewLifecycleOwner(), tasks -> {
            adapter.setNewTasks(tasks);
        });
    }
}