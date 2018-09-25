package edu.cuny.hunter.github.ui.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

public class ExtractChangeView extends ViewPart implements
		PropertyChangeListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.cuny.hunter.github.ui.views.ExtractChangeView";

	private ClearAction clearAction;

	private TreeViewer viewer;

	/**
	 * The constructor.
	 */
	public ExtractChangeView() {
		ExtractChangeUiPlugin.getDefault().setExtractChangeView(this);
		ExtractChangeUiPlugin.getDefault().addPropertyChangeListener(this);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	@Override
	public void createPartControl(Composite parent) {
		this.setViewer(new TreeViewer(parent));
		Tree tree = this.getViewer().getTree();
		final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		this.getViewer().getControl().setLayoutData(gridData);
		this.getViewer().setUseHashlookup(true);

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		TreeColumn treeColumn = new TreeColumn(tree, SWT.LEFT);
		//treeColumn.setText(this.ADVICE_COLUMN_NAME);
		treeColumn
				.setToolTipText("Advice whose pointcut is recommended to change.");

		treeColumn = new TreeColumn(tree, SWT.LEFT);
		//treeColumn.setText(this.CONFIDENCE_COLUMN_NAME);
		treeColumn.setToolTipText("The confidence in this pointcut changing.");
		treeColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {		
				// add action here
				//ExtractChangeView.this.getViewer()
				//		.setComparator(new ExtractChangeViewComparator(
				//		SortBy.CHANGE_CONFIDENCE));
			}
		});

		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(80));
		layout.addColumnData(new ColumnWeightData(20));

		tree.setLayout(layout);

		this.getViewer().setInput(getViewSite());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(this.clearAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.clearAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.clearAction);
	}

	public TreeViewer getViewer() {
		return this.viewer;
	}

	public void setViewer(TreeViewer viewer) {
		this.viewer = viewer;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ExtractChangeView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(this.getViewer().getControl());
		this.getViewer().getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, this.getViewer());
	}

	private void hookDoubleClickAction() {
		this.getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				//ExtractChangeView.this.doubleClickAction.run();
			}
		});
	}

	private void makeActions() {
		this.clearAction = new ClearAction();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		this.getViewer().getControl().setFocus();
	}

	public void propertyChange(PropertyChangeEvent evt) {
//
//			Display display = Display.getDefault();
//			display.asyncExec(new Runnable() {				
//				public void run() {
//					getViewer().refresh();
//				}
//			});
	}
}