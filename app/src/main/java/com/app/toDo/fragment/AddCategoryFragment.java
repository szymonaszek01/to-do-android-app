package com.app.toDo.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.app.toDo.R;
import com.app.toDo.configuration.DatabaseConfiguration;
import com.app.toDo.databinding.FragmentAddCategoryBinding;
import com.app.toDo.entity.Category;
import com.app.toDo.entity.Task;
import com.app.toDo.model.AppViewModel;
import com.app.toDo.service.CategoryService;
import com.app.toDo.service.TaskService;

import java.util.Optional;
import java.util.stream.Collectors;

public class AddCategoryFragment extends Fragment {

    private CategoryService categoryService;

    private TaskService taskService;

    private Category category;

    private FragmentAddCategoryBinding binding;

    private AppViewModel appViewModel;

    private String selectedCategory;

    public AddCategoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        categoryService = CategoryService.builder().categoryDao(DatabaseConfiguration.getInstance(getContext()).categoryDao()).build();
        taskService = TaskService.builder().taskDao(DatabaseConfiguration.getInstance(getContext()).taskDao()).build();

        binding = FragmentAddCategoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        category = appViewModel.getDefaultCategory();

        addCategoryButtonInit();
        deleteCategoryButtonInit();
        categorySelectViewInit();
        observeAppViewModel();

        return root;
    }

    public void addCategoryButtonInit() {
        binding.createCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    category.setName(binding.categoryNameView.getText().toString());
                    categoryService.addCategory(category);
                    appViewModel.setCategoryList(categoryService.getCategoryList());
                    if (appViewModel.getSelectedTask().getValue() != null) {
                        Navigation.findNavController(view).navigate(R.id.action_addCategoryFragment_to_editTaskFragment);
                    } else {
                        Navigation.findNavController(view).navigate(R.id.action_addCategoryFragment_to_addTaskFragment2);
                    }
                }
            }
        });
    }

    private void deleteCategoryButtonInit() {
        binding.deleteCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Optional<Category> category = categoryService.getCategoryByTitle(selectedCategory);
                Task task = appViewModel.getSelectedTask().getValue();
                if (!category.isPresent()) {
                    return;
                }
                if (task != null && category.get().getId() == task.getCategoryId()) {
                    binding.categorySelectView.setError("You can not remove category of updated task");
                    selectedCategory = "";
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to delete \"" + selectedCategory + "\" category?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Optional<Category> category = categoryService.getCategoryByTitle(selectedCategory);
                        if (!category.isPresent()) {
                            return;
                        }

                        categoryService.deleteCategory(category.get());
                        removeTaskWithDeletedCategory(category.get().getId());
                        appViewModel.setCategoryList(categoryService.getCategoryList());
                        appViewModel.setTaskList(taskService.getTaskList());
                        selectedCategory = "";
                        binding.categorySelectView.setText(selectedCategory);
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
            }
        });
    }

    private void removeTaskWithDeletedCategory(long categoryId) {
        taskService.getTaskList()
                .stream()
                .filter(task -> task.getCategoryId() == categoryId)
                .collect(Collectors.toList())
                .forEach(task -> taskService.deleteTask(task));
    }

    private void categorySelectViewInit() {
        binding.categorySelectView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.categorySelectView.setError(null);
                selectedCategory = s.toString();
                if (selectedCategory.isEmpty()) {
                    binding.deleteCategoryButton.setVisibility(View.INVISIBLE);
                } else {
                    binding.deleteCategoryButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void observeAppViewModel() {
        appViewModel.getCategoryList().observe(getViewLifecycleOwner(), categories -> {
            ArrayAdapter<String> adapter = new ArrayAdapter(requireContext(), R.layout.category_dropdown_list_item, categories);
            binding.categorySelectView.setAdapter(adapter);
        });
    }

    private boolean validateFields() {
        boolean isOk = true;
        if (binding.categoryNameView.getText().length() < 2) {
            isOk = false;
            binding.categoryNameView.setError("At least 2 signs please!");
        }
        if (categoryService.getCategoryByTitle(binding.categoryNameView.getText().toString()).isPresent()) {
            isOk = false;
            binding.categoryNameView.setError("Category exists!");
        }
        return isOk;
    }
}