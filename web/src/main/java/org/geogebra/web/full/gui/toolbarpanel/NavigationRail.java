package org.geogebra.web.full.gui.toolbarpanel;

import javax.annotation.CheckForNull;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.gui.AccessibilityGroup;
import org.geogebra.common.io.layout.DockPanelData.TabIds;
import org.geogebra.common.io.layout.PerspectiveDecoder;
import org.geogebra.web.full.css.MaterialDesignResources;
import org.geogebra.web.full.gui.exam.ExamLogAndExitDialog;
import org.geogebra.web.full.gui.menubar.FileMenuW;
import org.geogebra.web.html5.gui.util.AriaHelper;
import org.geogebra.web.html5.gui.util.NoDragImage;
import org.geogebra.web.html5.gui.view.button.StandardButton;
import org.geogebra.web.html5.gui.zoompanel.FocusableWidget;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.html5.util.Dom;
import org.geogebra.web.resources.SVGResource;
import org.geogebra.web.shared.GlobalHeader;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Navigation rail or bottom bar
 */
class NavigationRail extends FlowPanel {
	private MenuToggleButton btnMenu;
	private @CheckForNull StandardButton btnAlgebra;
	private @CheckForNull StandardButton btnTools;
	private @CheckForNull StandardButton btnTableView;
	private final Image imgMenu;
	private final FlowPanel contents;
	private FlowPanel center;
	private boolean animating = false;
	private boolean lastOrientation;

	/**
	 * application
	 */
	AppW app;
	/**
	 * Parent tool panel
	 */
	final ToolbarPanel toolbarPanel;
	private FocusableWidget focusableMenuButton;

	/**
	 * @param toolbarPanel
	 *            - panel containing the toolbar
	 */
	public NavigationRail(ToolbarPanel toolbarPanel) {
		this.app = toolbarPanel.getApp();
		this.toolbarPanel = toolbarPanel;
		contents = new FlowPanel();
		contents.addStyleName("contents");
		add(contents);
		if (app.getAppletParameters().getDataParamShowMenuBar(false)) {
			createMenuButton();
			toolbarPanel.setMenuButton(focusableMenuButton);
		}
		imgMenu = new NoDragImage(MaterialDesignResources.INSTANCE.toolbar_menu_black(), 24);
		createCenter();
		setLabels();
		setTabIndexes();
		lastOrientation = app.isPortrait();
		setStyleName("header");
	}

	private void createCenter() {
		if (!app.showToolBar()) {
			return;
		}

		center = new FlowPanel();
		center.addStyleName("center");

		createAlgebraButton();
		center.add(btnAlgebra);

		boolean showToolPanel = app.getConfig().showToolsPanel();

		if (showToolPanel) {
			createToolsButton();
			center.add(btnTools);
		}
		if (app.getConfig().hasTableView()) {
			createTableViewButton();
			center.add(btnTableView);
		}
		if (btnMenu != null && !isHeaderExternal()) {
			center.addStyleName("withMenu");
		}
		contents.add(center);
	}

	private void createAlgebraButton() {
		btnAlgebra = createTabButton("Algebra",
				MaterialDesignResources.INSTANCE.toolbar_algebra_graphing());
		btnAlgebra.addFastClickHandler(source -> onAlgebraPressed());
	}

	private void createToolsButton() {
		btnTools = createTabButton("Tools",
				MaterialDesignResources.INSTANCE.toolbar_tools());
		btnTools.addFastClickHandler(source -> onToolsPressed());
	}

	private void createTableViewButton() {
		btnTableView = createTabButton("Table",
				MaterialDesignResources.INSTANCE.toolbar_table_view_black());
		btnTableView.addFastClickHandler(source -> onTableViewPressed());
	}

	private StandardButton createTabButton(String label, SVGResource icon) {
		StandardButton btn = new StandardButton(icon, label, 24);
		btn.addStyleName("tabButton");
		AriaHelper.hide(btn);
		return btn;
	}

	/**
	 * Handler for Algebra button.
	 */
	protected void onAlgebraPressed() {
		if (isOpen() && toolbarPanel.getSelectedTabId() == TabIds.ALGEBRA) {
			onClosePressed(false);
			return;
		}
		toolbarPanel.openAlgebra(isOpen());
		app.setKeyboardNeeded(true);
		toolbarPanel.getFrame().keyBoardNeeded(false, null);
		toolbarPanel.getFrame().showKeyboardButton(true);
	}

	/**
	 * Handler for tools button.
	 */
	protected void onToolsPressed() {
		if (isOpen() && toolbarPanel.getSelectedTabId() == TabIds.TOOLS) {
			onClosePressed(false);
			return;
		}
		app.setKeyboardNeeded(false);
		toolbarPanel.getFrame().keyBoardNeeded(false, null);
		toolbarPanel.getFrame().showKeyboardButton(false);
		toolbarPanel.openTools(isOpen());
	}

	/**
	 * Handler for table view button.
	 */
	protected void onTableViewPressed() {
		if (isOpen() && toolbarPanel.getSelectedTabId() == TabIds.TABLE) {
			onClosePressed(false);
			return;
		}
		app.setKeyboardNeeded(false);
		toolbarPanel.getFrame().keyBoardNeeded(false, null);
		toolbarPanel.getFrame().showKeyboardButton(false);
		toolbarPanel.openTableView(null, isOpen());
	}

	/**
	 * Handler for Close button.
	 */
	protected void onClosePressed(boolean snap) {
		app.hideMenu();
		onClose(snap, ToolbarPanel.OPEN_ANIM_TIME);
		toolbarPanel.getFrame().showKeyBoard(false, null, true);
	}

	protected void onClose(boolean snap, int time) {
		updateIcons(null, app.isExamStarted());
		addCloseOrientationStyles();
		toolbarPanel.setMoveMode();
		toolbarPanel.close(snap, time);
		app.getAccessibilityManager().focusAnchorOrMenu();
	}

	private void addCloseOrientationStyles() {
		Dom.toggleClass(toolbarPanel, "closePortrait",
				"closeLandscape", app.isPortrait());
	}

	void removeCloseOrientationStyles() {
		toolbarPanel.removeStyleName("closePortrait");
		toolbarPanel.removeStyleName("closeLandscape");
	}

	/**
	 * set labels
	 */
	void setLabels() {
		setAltTexts();
	}

	private void setAltTexts() {
		imgMenu.setAltText(app.getLocalization().getMenu("Menu"));
		setButtonText(btnAlgebra, app.getConfig().getAVTitle());
		setButtonText(btnTools, "Tools");
		setButtonText(btnTableView, "Table");

	}

	private void setButtonText(StandardButton btnTools, String key) {
		if (btnTools != null) {
			btnTools.setText(app.getLocalization().getMenu(key));
		}
	}

	/**
	 * @param tabId
	 *            tab id
	 */
	void selectTab(TabIds tabId) {
		if (center == null) {
			return;
		}
		updateIcons(tabId, app.isExamStarted());
		toolbarPanel.setSelectedTabId(tabId);
	}

	private void setSelected(StandardButton btn, boolean selected, boolean exam) {
		if (btn != null) {
			GColor color = GColor.WHITE;
			if (!exam) {
				color = selected ? app.getVendorSettings().getPrimaryColor() : GColor.BLACK;
			}
			btn.setIcon(((SVGResource) btn.getIcon()).withFill(color.toString()));
			Dom.toggleClass(btn, "selected", selected);
		}
	}

	/**
	 * @param expanded
	 *            whether menu is expanded
	 */
	public void markMenuAsExpanded(boolean expanded) {
		if (btnMenu != null) {
			btnMenu.getElement().setAttribute("aria-expanded",
					String.valueOf(expanded));
			btnMenu.getElement().removeAttribute("aria-pressed");
			Dom.toggleClass(btnMenu, "selected", expanded);
		}
	}

	private void createMenuButton() {
		btnMenu = new MenuToggleButton(app);
		focusableMenuButton = new FocusableWidget(AccessibilityGroup.MENU, null, btnMenu);
		updateMenuPosition();
		markMenuAsExpanded(false);
	}

	private void updateMenuPosition() {
		if (btnMenu == null) {
			return;
		}
		boolean external = isHeaderExternal();
		btnMenu.setExternal(external);
		if (external) {
			btnMenu.addToGlobalHeader();
			addShareButton();
		} else {
			toolbarPanel.getFrame().add(btnMenu);
		}
	}

	private boolean isHeaderExternal() {
		return needsHeader() && GlobalHeader.isInDOM();
	}

	protected boolean needsHeader() {
		return !app.getAppletFrame().shouldHideHeader();
	}

	private void addShareButton() {
		GlobalHeader.INSTANCE.initShareButton(share -> {
			if (app.isMenuShowing()) {
				app.toggleMenu();
			}
			FileMenuW.share(app, share);
		});
	}

	/**
	 * Hide the entire undo/redo panel (eg. during animation).
	 */
	public void hideUndoRedoPanel() {
		toolbarPanel.showHideUndoRedoPanel(false);
	}

	/**
	 * @return - true if toolbar is open
	 */
	public boolean isOpen() {
		return toolbarPanel.isOpen();
	}

	private void removeOpenStyles() {
		removeStyleName("header-open-portrait");
		removeStyleName("header-open-landscape");
	}

	private void removeCloseStyles() {
		removeStyleName("header-close-portrait");
		removeStyleName("header-close-landscape");
	}

	/**
	 * update style of toolbar
	 */
	public void updateStyle() {
		if (isAnimating()) {
			return;
		}

		String orientation = app.isPortrait() ? "portrait" : "landscape";
		Dom.toggleClass(this, "compact",
				app.getAppletFrame().hasCompactNavigationRail());
		if (isOpen()) {
			removeCloseStyles();
			addStyleName("header-open-" + orientation);
			if (!app.isPortrait()) {
				clearHeight();
				clearWidth();
			}
		} else {
			removeOpenStyles();
			addStyleName("header-close-" + orientation);
			addCloseOrientationStyles();
		}

		updateMenuButtonStyle();

		toolbarPanel.updateUndoRedoPosition();
		toolbarPanel.updateUndoRedoActions();
		toolbarPanel.updateStyle();
	}

	private void updateMenuButtonStyle() {
		if (btnMenu == null) {
			return;
		}
		Dom.toggleClass(btnMenu, "portraitMenuBtn",
				"landscapeMenuBtn", app.isPortrait());
		btnMenu.getUpFace().setImage(imgMenu);
	}

	/**
	 * handle resize of toolbar
	 */
	public void resize() {
		if (isAnimating()) {
			return;
		}
		updateStyle();
	}

	/**
	 * @return true if animating
	 */
	public boolean isAnimating() {
		return animating;
	}

	/**
	 * @param b
	 *            - set if animating
	 */
	public void setAnimating(boolean b) {
		this.animating = b;
	}

	/**
	 * Resets toolbar.
	 */
	public void reset() {
		resize();
		toolbarPanel.updateUndoRedoPosition();
	}

	/**
	 * Called when app changes orientation.
	 */
	public void onOrientationChange() {
		if (lastOrientation != app.isPortrait()) {
			removeOpenStyles();
			removeCloseStyles();
		} else if (isOpen()) {
			removeCloseStyles();
		} else {
			removeOpenStyles();
		}
		updateStyle();

		lastOrientation = app.isPortrait();

		if (app.isPortrait()) {
			clearWidth();
			clearHeight();
			updateStyle();
		} else {
			if (!isOpen()) {
				int width = app.getAppletParameters().getDataParamWidth();
				if (app.getAppletParameters().getDataParamFitToScreen()) {
					width = Window.getClientWidth();
				}
				toolbarPanel.setLastOpenWidth((int) (width
						* PerspectiveDecoder.landscapeRatio(app, width)));
			}
		}
	}

	private void clearWidth() {
		getElement().getStyle().clearWidth();
	}

	private void clearHeight() {
		getElement().getStyle().clearHeight();
	}

	/**
	 * Sets tab order for header buttons.
	 */
	public void setTabIndexes() {
		if (btnMenu != null) {
			new FocusableWidget(AccessibilityGroup.MENU, null, btnMenu).attachTo(app);
		}
		if (focusableMenuButton != null) {
			focusableMenuButton.attachTo(app);
		}
		setAltTexts();
	}

	/** Sets focus to Burger menu */
	public void focusMenu() {
		if (btnMenu != null) {
			btnMenu.getElement().focus();
		}
	}

	/**
	 * After visibility changed in landscape
	 */
	public void onLandscapeAnimationEnd() {
		if (!isOpen()) {
			getElement().getStyle().clearWidth();
			toolbarPanel.updateUndoRedoPosition();
		} else {
			toolbarPanel.resizeTabs();
		}
		toolbarPanel.onResize();

		Scheduler.get().scheduleDeferred(() -> {
			toolbarPanel.showHideUndoRedoPanel(true);
			toolbarPanel.updateUndoRedoPosition();
			resize();
		});
	}

	/**
	 * Exam info button.
	 */
	public void initInfoBtnAction() {
		final StandardButton examInfoBtn = GlobalHeader.INSTANCE
				.getExamInfoBtn();
		if (examInfoBtn == null) {
			return;
		}
		examInfoBtn.addFastClickHandler(source -> showExamDialog(examInfoBtn));
	}

	private void showExamDialog(StandardButton examInfoBtn) {
		new ExamLogAndExitDialog(app, true, examInfoBtn).show();
	}

	public void updateIcons(boolean exam) {
		updateIcons(toolbarPanel.getSelectedTabId(), exam);
	}

	private void updateIcons(TabIds tabId, boolean exam) {
		setSelected(btnAlgebra, tabId == TabIds.ALGEBRA, exam);
		setSelected(btnTools, tabId == TabIds.TOOLS, exam);
		setSelected(btnTableView, tabId == TabIds.TABLE, exam);
	}

	public void setAVIconNonSelect(boolean exam) {
		setSelected(btnAlgebra, false, exam);
	}
}
