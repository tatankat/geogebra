///* 
//GeoGebra - Dynamic Mathematics for Everyone
//http://www.geogebra.org
//
//This file is part of GeoGebra.
//
//This program is free software; you can redistribute it and/or modify it 
//under the terms of the GNU General Public License as published by 
//the Free Software Foundation.
//
// */

package org.geogebra.web.full.gui.dialog.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.gui.dialog.ToolManagerDialogModel;
import org.geogebra.common.gui.dialog.ToolManagerDialogModel.ToolManagerDialogListener;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.main.Feature;
import org.geogebra.common.main.MyError.Errors;
import org.geogebra.common.move.ggtapi.models.Material.MaterialType;
import org.geogebra.web.full.css.MaterialDesignResources;
import org.geogebra.web.full.gui.GuiManagerW;
import org.geogebra.web.full.gui.dialog.DialogManagerW;
import org.geogebra.web.full.gui.util.SaveDialogI;
import org.geogebra.web.full.main.GeoGebraTubeExportW;
import org.geogebra.web.html5.gui.FastClickHandler;
import org.geogebra.web.html5.gui.util.LayoutUtilW;
import org.geogebra.web.html5.gui.util.ListBoxApi;
import org.geogebra.web.html5.gui.view.button.StandardButton;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.html5.main.LocalizationW;
import org.geogebra.web.resources.SVGResource;
import org.geogebra.web.shared.components.dialog.ComponentDialog;
import org.geogebra.web.shared.components.dialog.DialogData;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class ToolManagerDialogW extends ComponentDialog implements ToolManagerDialogListener,
		ToolNameIconPanelW.MacroChangeListener, MultiSelectButtonsPannel.ButtonsListener {

	AppW appw;
	final LocalizationW loc;
	private ToolManagerDialogModel model;

	MacroListBox toolList;

	private ToolNameIconPanelW macroPanel;

	private static class MacroListBox extends ListBox {
		List<Macro> macros;

		public MacroListBox() {
			macros = new ArrayList<>();
		}

		private static String getMacroText(Macro macro) {
			return macro.getToolName() + ": " + macro.getNeededTypesString();
		}

		public List<Macro> getMacros() {
			return macros;
		}

		public Macro getMacro(int index) {
			return macros.get(index);
		}

		public Macro getSelectedMacro() {
			int idx = getSelectedIndex();
			if (idx == -1) {
				return null;
			}
			return getMacro(idx);
		}

		public void setSelectedMacro(Macro macro) {
			int idx = getSelectedIndex();
			if (idx == -1) {
				return;
			}
			macros.set(idx, macro);
			setItemText(idx, getMacroText(macro));

		}

		public void addMacro(Macro macro) {
			macros.add(macro);
			addItem(getMacroText(macro));
		}

		public void insertMacro(Macro macro, int index) {
			macros.add(index, macro);
			insertItem(getMacroText(macro), index);
		}

		@Override
		public void removeItem(int index) {
			macros.remove(index);
			super.removeItem(index);

		}

		public List<Macro> getSelectedMacros() {
			List<Macro> sel = null;
			for (int i = 0; i < getItemCount(); i++) {
				if (isItemSelected(i)) {
					if (sel == null) {
						sel = new ArrayList<>();
					}
					sel.add(getMacro(i));
				}
			}

			return sel;
		}

		public boolean isEmpty() {
			return macros.isEmpty();
		}
	}

	/**
	 * @param app - application
	 * @param data - dialog data
	 */
	public ToolManagerDialogW(AppW app, DialogData data) {
		super(app, data, true, true);
		setModal(true);
		addStyleName("manageTools");
		model = new ToolManagerDialogModel(app, this);

		this.appw = app;
		this.loc = app.getLocalization();
		initGUI();
		setOnNegativeAction(this::applyChanges);
	}

	@Override
	public void setVisible(boolean flag) {
		if (flag) {
			appw.setMoveMode();
		}

		super.setVisible(flag);
	}

	/**
	 * Updates the order of macros.
	 */
	private void updateToolBar() {
		model.addMacros(toolList.getMacros().toArray());
		appw.updateToolBar();
	}

	@Override
	public void deleteSelection() {
		List<Integer> selIndexesTemp = ListBoxApi.getSelectionIndexes(toolList);
		if (selIndexesTemp.isEmpty()) {
			return;
		}
		StringBuilder macroNamesNoDel = new StringBuilder();
		StringBuilder macroNamesDel = new StringBuilder();

		for (int j = 0; j < selIndexesTemp.size(); j++) {
			int i = selIndexesTemp.get(j);
			if (toolList.getMacro(i).isUsed()) {
				macroNamesNoDel.append("\n"
						+ toolList.getMacro(i).getToolOrCommandName() + ": "
						+ toolList.getMacro(i).getNeededTypesString());
				toolList.setItemSelected(j, false);
			} else {
				macroNamesDel.append("\n"
						+ toolList.getMacro(i).getToolOrCommandName() + ": "
						+ toolList.getMacro(i).getNeededTypesString());
			}
		}
		if (macroNamesDel.length() == 0) {
			appw.showError(Errors.ToolDeleteUsed, macroNamesNoDel.toString());
		} else {
			String message = loc.getMenu("Tool.DeleteQuestion");

			if (macroNamesNoDel.length() != 0) {
				message += Errors.ToolDeleteUsed.getError(loc);
			}

			showDeleteToolDialog(message, macroNamesNoDel.toString());
		}
	}

	private void showDeleteToolDialog(String message, String macroNamesNoDelStr) {
		DialogData data = new DialogData(null, "Cancel", "Delete");
		ComponentDialog dialog = new ComponentDialog(appw, data, false, false);
		FlowPanel content = new FlowPanel();
		content.add(new Label(message));
		content.add(new Label(macroNamesNoDelStr));
		dialog.addDialogContent(content);
		dialog.setOnPositiveAction(this::onToolDelete);
		dialog.show();
	}

	private void onToolDelete() {
		final List<Integer> selIndexes = ListBoxApi
				.getSelectionIndexes(toolList);
		List<Macro> macros = toolList
				.getSelectedMacros();
		// need this because of removing

		Collections.reverse(selIndexes);
		for (Integer idx : selIndexes) {
			toolList.removeItem(idx);
		}

		if (!toolList.isEmpty()) {
			toolList.setSelectedIndex(0);
		} else {
			macroPanel.setMacro(null);
		}

		updateMacroPanel();
		if (model.deleteTools(macros)) {
			applyChanges();
			updateToolBar();
		}
	}

	private FlowPanel createListUpDownRemovePanel() {
		return new MultiSelectButtonsPannel(this);
	}

	private void addStyledButton(SVGResource img, FlowPanel rootPanel,
			String label, FastClickHandler clickHandler) {
		StandardButton btn = new StandardButton(img, label, 18);
		btn.addFastClickHandler(clickHandler);
		btn.addStyleName("containedButton");
		rootPanel.add(btn);
	}

	private void initGUI() {
		FlowPanel panel = new FlowPanel();

		Label lblTitle = new Label(loc.getMenu("Tools"));
		panel.add(lblTitle);

		toolList = new MacroListBox();
		toolList.setMultipleSelect(true);
		toolList.setVisibleItemCount(6);

		FlowPanel centerPanel = LayoutUtilW.panelRow(toolList,
				createListUpDownRemovePanel());
		centerPanel.setStyleName("multiSelectList");
		panel.add(centerPanel);

		FlowPanel toolButtonPanel = new FlowPanel();
		toolButtonPanel.addStyleName("toolButtons");
		panel.add(toolButtonPanel);

		if (appw.has(Feature.TOOL_EDITOR)) {
			addStyledButton(MaterialDesignResources.INSTANCE.mow_pdf_open_folder(),
					toolButtonPanel, loc.getMenu("Open"),
					w -> this.openTools());
		}

		addStyledButton(MaterialDesignResources.INSTANCE.save_black(), toolButtonPanel,
				loc.getMenu("Save"), w -> this.saveTools());
		addStyledButton(MaterialDesignResources.INSTANCE.share_black(), toolButtonPanel,
				loc.getMenu("Share"),
				w -> model.uploadToGeoGebraTube(toolList.getSelectedMacros().toArray()));

		// name & icon
		macroPanel = new ToolNameIconPanelW(appw);
		macroPanel.setMacroChangeListener(this);
		panel.add(macroPanel);

		insertTools();

		toolList.addChangeHandler(event -> updateMacroPanel());

		setDialogContent(panel);
	}

	private void updateMacroPanel() {
		macroPanel.setMacro(toolList.getSelectedMacro());
	}

	private void openTools() {
		appw.setWaitCursor();
		appw.storeMacro(toolList.getSelectedMacro(), false);
		appw.getFileManager().open(Window.Location.getHref(), "");

		appw.setDefaultCursor();
		hide();
	}

	private void insertTools() {
		toolList.clear();
		Kernel kernel = appw.getKernel();
		int size = kernel.getMacroNumber();

		for (int i = 0; i < size; i++) {
			Macro macro = kernel.getMacro(i);
			toolList.addMacro(macro);
		}
		toolList.setSelectedIndex(0);
		updateMacroPanel();
	}

	/**
	 * Saves all selected tools in a new file.
	 */
	private void saveTools() {
		applyChanges();
		SaveDialogI dlg = ((DialogManagerW) appw.getDialogManager())
				.getSaveDialog(false, true);
		dlg.setSaveType(MaterialType.ggt);
		dlg.show();
	}

	@Override
	public void removeMacroFromToolbar(int i) {
		appw.getGuiManager().removeFromToolbarDefinition(i);
	}

	@Override
	public void refreshCustomToolsInToolBar() {
		appw.getGuiManager().refreshCustomToolsInToolBar();
		appw.getGuiManager().updateToolbar();
	}

	@Override
	public void uploadWorksheet(ArrayList<Macro> macros) {
		GeoGebraTubeExportW exporter = new GeoGebraTubeExportW(appw);

		exporter.uploadWorksheet(macros);
	}

	@Override
	public void moveSelection(boolean up) {
		int idx = toolList.getSelectedIndex();
		if (idx == -1) {
			return;
		}

		List<Integer> sel = ListBoxApi.getSelectionIndexes(toolList);
		int selSize = sel.size();

		if (up) {
			if (idx > 0) {
				toolList.insertMacro(toolList.getMacro(idx - 1), idx + selSize);
				toolList.removeItem(idx - 1);
			}
		} else {
			if (idx + selSize < toolList.getItemCount()) {
				toolList.insertMacro(toolList.getMacro(idx + selSize), idx);
				toolList.removeItem(idx + selSize + 1);
			}
		}
	}

	private void applyChanges() {
		if (toolList.isEmpty()) {
			return;
		}

		model.addMacros(toolList.getMacros().toArray());

		appw.updateCommandDictionary();
		refreshCustomToolsInToolBar();
	}

	@Override
	public void onMacroChange(Macro macro) {
		Macro m = toolList.getSelectedMacro();
		m.setCommandName(macro.getCommandName());
		m.setToolName(macro.getToolName());
		m.setToolHelp(macro.getToolHelp());
		m.setIconFileName(macro.getIconFileName());
		m.setShowInToolBar(macro.isShowInToolBar());
		toolList.setSelectedMacro(m);
	}

	@Override
	public void onShowToolChange(Macro macro) {
		onMacroChange(macro);
		boolean active = macro.isShowInToolBar();
		Macro m = toolList.getSelectedMacro();

		if (active) {
			appw.getGuiManager().refreshCustomToolsInToolBar();
		} else {
			int macroID = m.getKernel().getMacroID(m)
					+ EuclidianConstants.MACRO_MODE_ID_OFFSET;
			appw.getGuiManager().removeFromToolbarDefinition(macroID);
		}
		GuiManagerW gm = (GuiManagerW) appw.getGuiManager();
		gm.setGeneralToolBarDefinition(gm.getCustomToolbarDefinition());
		updateToolBar();
	}
}
