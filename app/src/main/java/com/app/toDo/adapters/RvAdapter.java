package com.app.toDo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.app.toDo.R;
import com.app.toDo.configuration.DatabaseConfiguration;
import com.app.toDo.entity.Task;
import com.app.toDo.fragment.TasksFragmentDirections;
import com.app.toDo.service.CategoryService;
import com.app.toDo.util.DateConverter;

import java.util.List;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {

    private List<Task> tasks;

    private View context;

    private CategoryService categoryService;

    public RvAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.task_item, viewGroup, false);
        this.context = view;
        categoryService = CategoryService.builder().categoryDao(DatabaseConfiguration.getInstance(view.getContext()).categoryDao()).build();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        viewHolder.getTaskName().setText(tasks.get(position).getTitle());
        viewHolder.getTaskDesc().setText(tasks.get(position).getDesc());
        viewHolder.getTaskCategory().setText(getCategoryNameById(tasks.get(position).getCategoryId()));
        viewHolder.getTaskDateTime().setText(DateConverter.getPrettyLocalDateTime(tasks.get(position).getExecDateTimeEpoch()));
        viewHolder.getEditTaskButton().setOnClickListener(x -> {
            TasksFragmentDirections.ActionEditTaskAction action = TasksFragmentDirections.actionEditTaskAction();
            action.setTaskIdToEdit(tasks.get(position).getId());
            Navigation.findNavController(context).navigate(action);
        });
        if (tasks.get(position).getUri() != null) {
            viewHolder.getIsAttached().setImageResource(R.drawable.ic_baseline_attach_file_24);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setNewTasks(List<Task> tasks) {
        this.tasks = tasks;
        this.notifyDataSetChanged();
    }

    private String getCategoryNameById(long categoryId) {
        return categoryService.getCategoryNameById(categoryId);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView taskName;

        private final TextView taskDesc;

        private final TextView taskCategory;

        private final TextView taskDateTime;

        private final Button editTaskButton;

        private final ImageView isAttached;

        public ViewHolder(View view) {
            super(view);

            taskName = view.findViewById(R.id.task_name);
            taskDesc = view.findViewById(R.id.task_desc);
            taskCategory = view.findViewById(R.id.task_category);
            taskDateTime = view.findViewById(R.id.task_datetime);
            editTaskButton = view.findViewById(R.id.edit_button);
            isAttached = view.findViewById(R.id.is_attached);
        }

        public TextView getTaskName() {
            return taskName;
        }

        public TextView getTaskDesc() {
            return taskDesc;
        }

        public TextView getTaskCategory() {
            return taskCategory;
        }

        public TextView getTaskDateTime() {
            return taskDateTime;
        }

        public Button getEditTaskButton() {
            return editTaskButton;
        }

        public ImageView getIsAttached() {
            return isAttached;
        }

    }
}
