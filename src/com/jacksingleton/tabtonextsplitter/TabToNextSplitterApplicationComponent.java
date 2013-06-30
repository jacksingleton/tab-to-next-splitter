package com.jacksingleton.tabtonextsplitter;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorWithProviderComposite;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class TabToNextSplitterApplicationComponent implements ProjectComponent, EditorFactoryListener {

    private Document lastEditorClosed;
    private Document editorBeingMoved;

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        System.out.println("=================================");
        System.out.println("EDITOR CREATED");
        Document document = event.getEditor().getDocument();
        if (document == lastEditorClosed || document == editorBeingMoved) {
            System.out.println("EDITOR MOVED");
        } else {
            editorBeingMoved = document;
            final Project project = event.getEditor().getProject();
            final FileEditorManagerEx fileEditorManager = FileEditorManagerEx.getInstanceEx(project);
            final AsyncResult<EditorWindow> activeWindowFuture = fileEditorManager.getActiveWindow();
            EditorWindow activeWindowPane = null;
            while (!activeWindowFuture.isDone());
            activeWindowPane = activeWindowFuture.getResult();

            if (activeWindowPane == null) return; // Action invoked when no files are open; do nothing

            final EditorWindow nextWindowPane = fileEditorManager.getNextWindow(activeWindowPane);

            if (nextWindowPane == activeWindowPane) return; // Action invoked with one pane open; do nothing

            final EditorWithProviderComposite activeEditorTab = activeWindowPane.getSelectedEditor();
            final VirtualFile activeFile = activeEditorTab.getFile();

            nextWindowPane.getManager().openFileImpl2(nextWindowPane, activeFile, true);

            activeWindowPane.closeFile(activeFile, true, false);
        }
        System.out.println("=================================");
    }

    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        System.out.println("=================================");
        System.out.println("EDITOR CLOSED");
        lastEditorClosed = event.getEditor().getDocument();
        System.out.println("=================================");
    }

    @Override
    public void initComponent() {
        EditorFactory.getInstance().addEditorFactoryListener(this);

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "TabToNextSplitter";
    }



    @Override public void projectOpened() { }

    @Override public void projectClosed() { }

    @Override public void disposeComponent() { }

}
