package com.app.toDo.fragment;

import android.os.Bundle;
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
import com.app.toDo.model.AppViewModel;
import com.app.toDo.service.CategoryService;

public class AddCategoryFragment extends Fragment {

    private CategoryService categoryService;

    private Category category;

    private FragmentAddCategoryBinding binding;

    private AppViewModel appViewModel;

    public AddCategoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        categoryService = CategoryService.builder().categoryDao(DatabaseConfiguration.getInstance(getContext()).categoryDao()).build();

        binding = FragmentAddCategoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        category = appViewModel.getDefaultCategory();

        addCategoryButtonInit();
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

                    Navigation.findNavController(view).navigate(R.id.action_addCategoryFragment_to_addTaskFragment2);
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
            binding.categoryNameView.setError("Atleast 2 signs please!");
        }
        return isOk;
    }
}