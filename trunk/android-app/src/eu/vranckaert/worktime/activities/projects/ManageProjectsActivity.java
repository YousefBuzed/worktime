/*
 *  Copyright 2011 Dirk Vranckaert
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.vranckaert.worktime.activities.projects;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.comparators.project.ProjectByNameComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TrackerConstants;
import eu.vranckaert.worktime.exceptions.AtLeastOneProjectRequiredException;
import eu.vranckaert.worktime.exceptions.ProjectStillInUseException;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.Task;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TaskService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.WidgetService;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.punchbar.PunchBarUtil;
import eu.vranckaert.worktime.utils.tracker.AnalyticsTracker;
import roboguice.activity.GuiceListActivity;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 18:19
 */
public class ManageProjectsActivity extends GuiceListActivity {
    private static final String LOG_TAG = ManageProjectsActivity.class.getSimpleName();

    private List<Project> projects;
    private List<Project> unfinishedProjects;

    private Project projectToRemove = null;
    private Project projectToUpdate = null;

    @Inject
    private ProjectService projectService;

    @Inject
    private WidgetService widgetService;
    
    @Inject
    private TaskService taskService;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @InjectView(R.id.showHideFinishedProjectButton)
    private ImageButton showHideFinishedProjectButton;

    private AnalyticsTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_projects);
        tracker = AnalyticsTracker.getInstance(getApplicationContext());
        tracker.trackPageView(TrackerConstants.PageView.MANAGE_PROJECTS_ACTIVITY);

        loadProjects();

        getListView().setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "Clicked on project-item " + position);
                Project selectedProject = projects.get(position);
                openProjectDetailActivity(selectedProject);
            }
        });

        registerForContextMenu(getListView());
    }

    /**
     * Load the all the projects to display and attach the listAdapater.
     */
    private void loadProjects() {
        clearProjects();

        setShowHideFinishedTasksButton();

        boolean hide = Preferences.getDisplayProjectsHideFinished(getApplicationContext());
        if (hide) {
            this.projects = projectService.findUnfinishedProjects();
            this.unfinishedProjects = projects;
        } else {
            this.projects = projectService.findAll();
            this.unfinishedProjects = projectService.findUnfinishedProjects();
        }
        Collections.sort(this.projects, new ProjectByNameComparator());
        Log.d(LOG_TAG, projects.size() + " projects loaded!");
        ManageProjectsListAdapter adapter = new ManageProjectsListAdapter(projects);
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }

    /**
     * Clear the list of projects.
     */
    private void clearProjects() {
        if (this.projects == null) {
            this.projects = new ArrayList<Project>();
        }
        this.projects.clear();
        ManageProjectsListAdapter adapter = new ManageProjectsListAdapter(projects);
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }

    private void setShowHideFinishedTasksButton() {
        boolean hide = Preferences.getDisplayProjectsHideFinished(ManageProjectsActivity.this);

        if (hide) {
            showHideFinishedProjectButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_title_locked));
        } else {
            showHideFinishedProjectButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_title_unlocked));
        }
    }

    /**
     * Delete a project.
     * @param project The project to delete.
     * @param askConfirmation If a confirmation should be requested to the user. If so the delete will no be executed
     * but a show dialog is called form where you have to call this method again with the askConfirmation parameter set
     */
    private void deleteProject(Project project, boolean askConfirmation) {
        if (askConfirmation) {
            Log.d(LOG_TAG, "Asking confirmation to remove a project");
            projectToRemove = project;
            showDialog(Constants.Dialog.DELETE_PROJECT_YES_NO);
        } else {
            try {
                Log.d(LOG_TAG, "Ready to actually remove the project!");
                projectService.remove(project);
                tracker.trackEvent(
                        TrackerConstants.EventSources.MANAGE_PROJECTS_ACTIVITY,
                        TrackerConstants.EventActions.DELETE_PROJECT
                );
                Log.d(LOG_TAG, "Project removed, ready to reload projects");
                loadProjects();
                projectToRemove = null;
            } catch (AtLeastOneProjectRequiredException e) {
                Toast.makeText(ManageProjectsActivity.this, R.string.msg_delete_project_at_least_one_required,  Toast.LENGTH_LONG).show();
            } catch (ProjectStillInUseException e) {
                showDialog(Constants.Dialog.WARN_PROJECT_DELETE_PROJECT_STILL_IN_USE);
            }
        }
    }

    /**
     * Method to switch the finished-flag of a {@link Project}.
     * @param project The project to switch on.
     * @param askConfirmationWhenRemainingUnfinishedTasks If set to {@link Boolean#TRUE} this flag will cause to ask 
     * for confirmation only if the project is not yet finished and it has tasks that are not yet finished!
     */
    private void switchProjectFinished(Project project, boolean askConfirmationWhenRemainingUnfinishedTasks) {
        List<Task> unfinishedTasks = new ArrayList<Task>();
        if (askConfirmationWhenRemainingUnfinishedTasks) {
            unfinishedTasks = taskService.findNotFinishedTasksForProject(project);
        }
        
        if (!project.isFinished() && unfinishedTasks.size() > 0) {
            Log.d(LOG_TAG, "Asking confirmation to finish a project");
            projectToUpdate = project;
            showDialog(Constants.Dialog.ASK_FINISH_PROJECT_WITH_REMAINING_UNFINISHED_TASKS);
        } else {
            if (!project.isFinished()) { //Project will be marked as finished
                TimeRegistration ongoingTR = timeRegistrationService.getLatestTimeRegistration();
                if (ongoingTR != null && ongoingTR.isOngoingTimeRegistration()) {
                    taskService.refresh(ongoingTR.getTask());
                    if (ongoingTR.getTask().getProject().getId().equals(project.getId())) {
                        showDialog(Constants.Dialog.WARN_PROJECT_NOT_FINISHED_ONGOING_TR);
                        return;
                    }
                }
            }
            
            project.setFinished(!project.isFinished());
            projectService.update(project);
            projectToUpdate = null;

            if (project.isFinished()) { // Project is marked as finished
                Project defaultProject = projectService.changeDefaultProjectUponProjectMarkedFinished(project);
                Project selectedWidgetProject = projectService.getSelectedProject();
                if (selectedWidgetProject.getId().equals(project.getId())) {
                    projectService.setSelectedProject(defaultProject);
                    widgetService.updateWidget(ManageProjectsActivity.this);
                }
            }
            
            loadProjects();
        }
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch(dialogId) {
            case Constants.Dialog.DELETE_PROJECT_YES_NO: {
                AlertDialog.Builder alertRemoveAllRegs = new AlertDialog.Builder(this);
				alertRemoveAllRegs.setTitle(projectToRemove.getName())
						   .setMessage(R.string.msg_delete_project_confirmation)
						   .setCancelable(false)
						   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									deleteProject(projectToRemove, false);
                                    removeDialog(Constants.Dialog.DELETE_PROJECT_YES_NO);
								}
							})
						   .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
                                    projectToRemove = null;
									removeDialog(Constants.Dialog.DELETE_PROJECT_YES_NO);
								}
							});
				dialog = alertRemoveAllRegs.create();
                break;
            }
            case Constants.Dialog.WARN_PROJECT_DELETE_PROJECT_STILL_IN_USE: {
                AlertDialog.Builder alertRemoveProjectNotPossible = new AlertDialog.Builder(this);
				alertRemoveProjectNotPossible.setTitle(projectToRemove.getName())
						   .setMessage(R.string.msg_delete_task_unavailable_still_in_use)
						   .setCancelable(false)
						   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   projectToRemove = null;
                                   removeDialog(Constants.Dialog.WARN_PROJECT_DELETE_PROJECT_STILL_IN_USE);
                               }
                           });
				dialog = alertRemoveProjectNotPossible.create();
                break;
            }
            case Constants.Dialog.ASK_FINISH_PROJECT_WITH_REMAINING_UNFINISHED_TASKS: {
                AlertDialog.Builder alertFinishProject = new AlertDialog.Builder(this);
                alertFinishProject.setTitle(projectToUpdate.getName())
                        .setMessage(R.string.msg_mark_project_finished_with_unfinished_tasks_confirmation)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switchProjectFinished(projectToUpdate, false);
                                removeDialog(Constants.Dialog.ASK_FINISH_PROJECT_WITH_REMAINING_UNFINISHED_TASKS);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                projectToUpdate = null;
                                removeDialog(Constants.Dialog.ASK_FINISH_PROJECT_WITH_REMAINING_UNFINISHED_TASKS);
                            }
                        });
                dialog = alertFinishProject.create();
                break;
            }
            case Constants.Dialog.WARN_PROJECT_NOT_FINISHED_ONGOING_TR: {
                AlertDialog.Builder warnProjectNotFinishedOngoingTr = new AlertDialog.Builder(this);
                warnProjectNotFinishedOngoingTr
                        .setMessage(R.string.msg_mark_project_finished_not_possible_ongoing_tr)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.WARN_PROJECT_NOT_FINISHED_ONGOING_TR);
                            }
                        });
                dialog = warnProjectNotFinishedOngoingTr.create();
                break;
            }
        };
        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( (requestCode == Constants.IntentRequestCodes.ADD_PROJECT
                || requestCode == Constants.IntentRequestCodes.EDIT_PROJECT)
                && resultCode == RESULT_OK) {
            Log.d(LOG_TAG, "A new project is added or an existing one is edited which requires a reload of the project list!");
            loadProjects();
        }
        if (requestCode == Constants.IntentRequestCodes.PROJECT_DETAILS && resultCode == RESULT_OK) {
            Log.d(LOG_TAG, "A project has been updated on the project details view, it's necessary to reload the list of project upon return!");
            loadProjects();
        }
        if (requestCode == Constants.IntentRequestCodes.PUNCH_BAR_START_TIME_REGISTRATION
                || requestCode == Constants.IntentRequestCodes.PUNCH_BAR_END_TIME_REGISTRATION) {
            PunchBarUtil.configurePunchBar(ManageProjectsActivity.this, timeRegistrationService, taskService, projectService);
        }
    }

    /**
     * The list adapater private inner-class used to display the manage projects list.
     */
    private class ManageProjectsListAdapter extends ArrayAdapter<Project> {
        private final String LOG_TAG = ManageProjectsListAdapter.class.getSimpleName();
        /**
         * {@inheritDoc}
         */
        public ManageProjectsListAdapter(List<Project> projects) {
            super(ManageProjectsActivity.this, R.layout.list_item_projects, projects);
            Log.d(LOG_TAG, "Creating the manage projects list adapater");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(LOG_TAG, "Start rendering/recycling row " + position);
            View row;
            final Project project = projects.get(position);
            Log.d(LOG_TAG, "Got project with name " + project.getName());
            Log.d(LOG_TAG, "Is this project the default project?" + project.isDefaultValue());

            if (convertView == null) {
                Log.d(LOG_TAG, "Render a new line in the list");
                row = getLayoutInflater().inflate(R.layout.list_item_projects, parent, false);
            } else {
                Log.d(LOG_TAG, "Recycling an existing line in the list");
                row = convertView;
            }

            Log.d(LOG_TAG, "Ready to update the name of the project of the listitem...");
            TextView projectname = (TextView) row.findViewById(R.id.projectname_listitem);
            projectname.setText(project.getName());

            Log.d(LOG_TAG, "Render the finished image if the project is finished");
            View finishedImg = row.findViewById(R.id.img_finished);
            if (project.isFinished()) {
                finishedImg.setVisibility(View.VISIBLE);
            } else {
                finishedImg.setVisibility(View.GONE);
            }

            Log.d(LOG_TAG, "Done rendering row " + position);
            return row;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(LOG_TAG, "In method onCreateContextMenu(...)");
        if (v.getId() == android.R.id.list) {
            super.onCreateContextMenu(menu, v, menuInfo);
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            int element = info.position;
            Log.d(LOG_TAG, "Creating context menu for element " + element + " in list");
            Project projectForContext = projects.get(element);

            menu.setHeaderTitle(projectForContext.getName());

            menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.PROJECT_DETAILS,
                    Menu.NONE,
                    R.string.lbl_projects_menu_details
            );
            menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.PROJECT_EDIT,
                    Menu.NONE,
                    R.string.lbl_projects_menu_edit
            );
            if (projects.size() > 1) {
                menu.add(Menu.NONE,
                        Constants.ContentMenuItemIds.PROJECT_DELETE,
                        Menu.NONE,
                        R.string.lbl_projects_menu_delete
                );
            }

            if (!projectForContext.isFinished()) {
                if (unfinishedProjects.size() > 1) {
                    menu.add(Menu.NONE,
                            Constants.ContentMenuItemIds.PROJECT_MARK_FINISHED,
                            Menu.NONE,
                            R.string.lbl_projects_menu_mark_finished
                    );
                }
            } else {
                menu.add(Menu.NONE,
                        Constants.ContentMenuItemIds.PROJECT_MARK_UNFINISHED,
                        Menu.NONE,
                        R.string.lbl_projects_menu_mark_unfinished
                );
            }

            menu.add(Menu.NONE,
                    Constants.ContentMenuItemIds.PROJECT_ADD,
                    Menu.NONE,
                    R.string.lbl_projects_menu_add
            );
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int element = info.position;

        Project projectForContext = projects.get(element);
        switch (item.getItemId()) {
            case Constants.ContentMenuItemIds.PROJECT_DETAILS: {
                openProjectDetailActivity(projectForContext);
                break;
            }
            case Constants.ContentMenuItemIds.PROJECT_DELETE: {
                deleteProject(projectForContext, true);
                break;
            }
            case Constants.ContentMenuItemIds.PROJECT_MARK_FINISHED: {
                switchProjectFinished(projectForContext, true);
                break;
            }
            case Constants.ContentMenuItemIds.PROJECT_MARK_UNFINISHED: {
                switchProjectFinished(projectForContext, false);
                break;
            }
            case Constants.ContentMenuItemIds.PROJECT_ADD: {
                openAddProjectActivity();
                break;
            }
            case Constants.ContentMenuItemIds.PROJECT_EDIT: {
                openEditProjectActivity(projectForContext);
                break;
            }
            default: {
                return false;
            }
        }

        return true;
    }

    /**
     * Opens the edit project activity. The activity is started for result!
     * @param project The project to edit.
     */
    private void openEditProjectActivity(Project project) {
        Intent intent = new Intent(getApplicationContext(), AddEditProjectActivity.class);
        intent.putExtra(Constants.Extras.PROJECT, project);
        startActivityForResult(intent, Constants.IntentRequestCodes.EDIT_PROJECT);
    }

    /**
     * Opens the project detail activity.
     * @param selectedProject A {@link Project} instance.
     */
    private void openProjectDetailActivity(Project selectedProject) {
        Intent projectDetails = new Intent(this, ProjectDetailsActivity.class);
        Log.d(LOG_TAG, "Putting project with name '" + selectedProject.getName() + "' on intent");
        projectDetails.putExtra(Constants.Extras.PROJECT, selectedProject);
        startActivityForResult(projectDetails, Constants.IntentRequestCodes.PROJECT_DETAILS);
    }

    /**
     * Opens the activity to add a new project. Activity is started for result!
     */
    private void openAddProjectActivity() {
        Intent intent = new Intent(getApplicationContext(), AddEditProjectActivity.class);
        startActivityForResult(intent, Constants.IntentRequestCodes.ADD_PROJECT);
    }

    /**
     * Navigate home.
     * @param view The view.
     */
    public void onHomeClick(View view) {
        IntentUtil.goHome(this);
    }

    public void showHideFinishedProjects(View view) {
        boolean hide = Preferences.getDisplayProjectsHideFinished(ManageProjectsActivity.this);
        Preferences.setDisplayProjectsHideFinished(ManageProjectsActivity.this, !hide);

        loadProjects();
    }

    /**
     * Navigate to the add projects activity.
     * @param view The view.
     */
    public void onAddClick(View view) {
        openAddProjectActivity();
    }

    public void onPunchButtonClick(View view) {
        PunchBarUtil.onPunchButtonClick(ManageProjectsActivity.this, timeRegistrationService);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PunchBarUtil.configurePunchBar(ManageProjectsActivity.this, timeRegistrationService, taskService, projectService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopSession();
    }
}