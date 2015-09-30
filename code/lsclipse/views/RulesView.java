/*     */ package lsclipse.views;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import lsclipse.LSDResult;
/*     */ import lsclipse.LSDiffRunner;
/*     */ import lsclipse.LSclipse;
/*     */ import lsclipse.dialogs.ProgressBarDialog;
/*     */ import lsclipse.dialogs.SelectProjectDialog;
/*     */ import org.eclipse.core.resources.IFile;
/*     */ import org.eclipse.core.resources.IMarker;
/*     */ import org.eclipse.core.resources.IProject;
/*     */ import org.eclipse.core.resources.IWorkspace;
/*     */ import org.eclipse.core.resources.IWorkspaceRoot;
/*     */ import org.eclipse.core.resources.ResourcesPlugin;
/*     */ import org.eclipse.jface.action.Action;
/*     */ import org.eclipse.jface.action.IToolBarManager;
/*     */ import org.eclipse.swt.events.MouseEvent;
/*     */ import org.eclipse.swt.layout.GridData;
/*     */ import org.eclipse.swt.layout.GridLayout;
/*     */ import org.eclipse.swt.widgets.Composite;
/*     */ import org.eclipse.swt.widgets.Event;
/*     */ import org.eclipse.swt.widgets.TabFolder;
/*     */ import org.eclipse.swt.widgets.TabItem;
/*     */ import org.eclipse.swt.widgets.Table;
/*     */ import org.eclipse.swt.widgets.TableColumn;
/*     */ import org.eclipse.swt.widgets.TableItem;
/*     */ import org.eclipse.ui.IActionBars;
/*     */ import org.eclipse.ui.IWorkbench;
/*     */ import org.eclipse.ui.PlatformUI;
/*     */ 
/*     */ public class RulesView extends org.eclipse.ui.part.ViewPart
/*     */ {
/*     */   Action selectAction;
/*     */   Action explainAction;
/*     */   Action englishAction;
/*     */   Action filterAction;
/*     */   Action sortAction;
/*     */   TabFolder tabFolder;
/*     */   GridData layoutData1;
/*     */   GridData layoutHidden;
/*     */   Composite parent;
/*     */   Table rulesTable;
/*     */   TabItem tabItemExamples;
/*     */   TabItem tabItemExceptions;
/*     */   org.eclipse.swt.widgets.List examplesList;
/*     */   org.eclipse.swt.widgets.List exceptionsList;
/*     */   ProgressBarDialog progbar;
/*  50 */   java.util.List<LSDResult> rules = new ArrayList();
/*  51 */   IProject baseproj = null;
/*  52 */   IProject newproj = null;
/*     */   
/*     */   public void createPartControl(Composite parent) {
/*  55 */     this.parent = parent;
/*     */     
/*  57 */     GridLayout layout = new GridLayout();
/*  58 */     layout.numColumns = 2;
/*  59 */     parent.setLayout(layout);
/*     */     
/*     */ 
/*  62 */     this.layoutData1 = new GridData(2);
/*  63 */     this.layoutData1.grabExcessHorizontalSpace = true;
/*  64 */     this.layoutData1.grabExcessVerticalSpace = true;
/*  65 */     this.layoutData1.horizontalAlignment = 4;
/*  66 */     this.layoutData1.verticalAlignment = 4;
/*  67 */     this.layoutData1.exclude = false;
/*     */     
/*     */ 
/*  70 */     this.layoutHidden = new GridData(2);
/*  71 */     this.layoutHidden.grabExcessHorizontalSpace = true;
/*  72 */     this.layoutHidden.grabExcessVerticalSpace = true;
/*  73 */     this.layoutHidden.horizontalAlignment = 4;
/*  74 */     this.layoutHidden.verticalAlignment = 4;
/*  75 */     this.layoutHidden.exclude = true;
/*     */     
/*     */ 
/*  78 */     this.rulesTable = new Table(parent, 65540);
/*  79 */     TableColumn col1 = new TableColumn(this.rulesTable, 0);
/*  80 */     this.rulesTable.setHeaderVisible(true);
/*  81 */     col1.setText("Accuracy");
/*  82 */     col1.pack();
/*  83 */     TableColumn col2 = new TableColumn(this.rulesTable, 0);
/*  84 */     col2.setText("Rule");
/*  85 */     col2.setWidth(430);
/*  86 */     this.rulesTable.setLayoutData(this.layoutData1);
/*  87 */     this.rulesTable.addListener(13, new org.eclipse.swt.widgets.Listener() {
/*     */       public void handleEvent(Event e) {
/*  89 */         RulesView.this.refreshExamples();
/*     */       }
/*     */       
/*     */ 
/*  93 */     });
/*  94 */     this.tabFolder = new TabFolder(parent, 0);
/*  95 */     this.tabItemExamples = new TabItem(this.tabFolder, 0);
/*  96 */     this.tabItemExamples.setText("Changes");
/*  97 */     this.examplesList = new org.eclipse.swt.widgets.List(this.tabFolder, 4);
/*  98 */     this.examplesList.addMouseListener(new org.eclipse.swt.events.MouseListener()
/*     */     {
/*     */       public void mouseDoubleClick(MouseEvent arg0)
/*     */       {
/* 102 */         org.eclipse.jface.dialogs.MessageDialog.openError(RulesView.this.examplesList.getShell(), "File selection error", RulesView.this.examplesList.getSelection()[0]);
/*     */       }
/*     */       
/*     */       public void mouseDown(MouseEvent arg0) {}
/*     */       
/*     */       public void mouseUp(MouseEvent arg0) {}
/* 108 */     });
/* 109 */     this.tabItemExamples.setControl(this.examplesList);
/* 110 */     this.tabItemExceptions = new TabItem(this.tabFolder, 0);
/* 111 */     this.tabItemExceptions.setText("Exceptions");
/* 112 */     this.exceptionsList = new org.eclipse.swt.widgets.List(this.tabFolder, 4);
/* 113 */     this.tabItemExceptions.setControl(this.exceptionsList);
/* 114 */     this.tabFolder.setLayoutData(this.layoutHidden);
/*     */     
/* 116 */     parent.layout();
/*     */     
/* 118 */     createActions();
/* 119 */     createMenu();
/* 120 */     createToolbar();
/*     */   }
/*     */   
/*     */   public void createActions() {
/* 124 */     this.selectAction = new Action("Select version...")
/*     */     {
/*     */       public void run()
/*     */       {
/* 128 */         SelectProjectDialog seldiag = new SelectProjectDialog(RulesView.this.parent.getShell());
/* 129 */         int returncode = seldiag.open();
/* 130 */         if (returncode > 0) { return;
/*     */         }
/*     */         
/* 133 */         RulesView.this.baseproj = ResourcesPlugin.getWorkspace().getRoot().getProject(seldiag.getProj1());
/* 134 */         RulesView.this.newproj = ResourcesPlugin.getWorkspace().getRoot().getProject(seldiag.getProj2());
/*     */         
/*     */ 
/* 137 */         ProgressBarDialog pbdiag = new ProgressBarDialog(RulesView.this.parent.getShell());
/* 138 */         pbdiag.open();
/* 139 */         pbdiag.setStep(0);
/*     */         
/*     */ 
/* 142 */         RulesView.this.rules = new LSDiffRunner().doLSDiff(seldiag.getProj1(), seldiag.getProj2(), pbdiag);
/*     */         
/*     */ 
/* 145 */         RulesView.this.refreshRules();
/* 146 */         RulesView.this.refreshExamples();
/*     */       }
/* 148 */     };
/* 149 */     this.selectAction.setImageDescriptor(PlatformUI.getWorkbench()
/* 150 */       .getSharedImages().getImageDescriptor(
/* 151 */       "IMG_OBJ_FOLDER"));
/*     */     
/*     */ 
/* 154 */     this.explainAction = new Action("Explain") {
/*     */       public void run() {
/* 156 */         if (RulesView.this.tabFolder.getLayoutData().equals(RulesView.this.layoutHidden)) {
/* 157 */           RulesView.this.showRulesList();
/*     */         } else {
/* 159 */           RulesView.this.hideRulesList();
/*     */         }
/*     */       }
/* 162 */     };
/* 163 */     this.explainAction.setImageDescriptor(LSclipse.getImageDescriptor("icons/explain.gif"));
/*     */     
/*     */ 
/* 166 */     this.englishAction = new Action("Translate to English")
/*     */     {
/*     */       public void run() {}
/*     */ 
/* 170 */     };
/* 171 */     this.englishAction.setImageDescriptor(LSclipse.getImageDescriptor("icons/english.gif"));
/*     */     
/*     */ 
/*     */ 
/* 175 */     this.sortAction = new Action("Sort")
/*     */     {
/*     */       public void run() {}
/* 178 */     };
/* 179 */     this.sortAction.setImageDescriptor(LSclipse.getImageDescriptor("icons/sort.gif"));
/*     */     
/*     */ 
/* 182 */     this.filterAction = new Action("Filter")
/*     */     {
/*     */       public void run() {}
/* 185 */     };
/* 186 */     this.filterAction.setImageDescriptor(LSclipse.getImageDescriptor("icons/filter.gif"));
/*     */   }
/*     */   
/*     */   private void createToolbar() {
/* 190 */     IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
/* 191 */     mgr.add(this.selectAction);
/* 192 */     mgr.add(this.explainAction);
/* 193 */     mgr.add(this.englishAction);
/* 194 */     mgr.add(this.sortAction);
/* 195 */     mgr.add(this.filterAction);
/*     */   }
/*     */   
/*     */ 
/*     */   private void createMenu() {}
/*     */   
/*     */ 
/*     */   public void setFocus() {}
/*     */   
/*     */   private void showRulesList()
/*     */   {
/* 206 */     this.tabFolder.setLayoutData(this.layoutData1);
/* 207 */     this.tabFolder.layout();
/* 208 */     this.parent.layout();
/*     */   }
/*     */   
/* 211 */   private void hideRulesList() { this.tabFolder.setLayoutData(this.layoutHidden);
/* 212 */     this.tabFolder.layout();
/* 213 */     this.parent.layout();
/*     */   }
/*     */   
/*     */   private void refreshRules() {
/* 217 */     this.rulesTable.removeAll();
/* 218 */     for (int i = 0; i < this.rules.size(); i++) {
/* 219 */       LSDResult rule = (LSDResult)this.rules.get(i);
/* 220 */       TableItem ti = new TableItem(this.rulesTable, 0);
/* 221 */       ti.setText(new String[] { rule.num_matches + "/" + (rule.num_matches + rule.num_counter), rule.desc });
/*     */     }
/* 223 */     this.rulesTable.layout();
/*     */   }
/*     */   
/*     */   private void refreshExamples() {
/* 227 */     refreshExamples(this.rulesTable.getSelectionIndex());
/*     */   }
/*     */   
/*     */   private void refreshExamples(int index)
/*     */   {
/* 232 */     if ((index < 0) || (index >= this.rules.size())) { return;
/*     */     }
/* 234 */     LSDResult rule = (LSDResult)this.rules.get(index);
/*     */     
/*     */ 
/* 237 */     this.tabItemExamples.setText("Changes (" + rule.examples.size() + ")");
/* 238 */     this.examplesList.removeAll();
/* 239 */     String s; for (Iterator localIterator = rule.getExampleStr().iterator(); localIterator.hasNext(); this.examplesList.add(s)) { s = (String)localIterator.next();
/*     */     }
/*     */     
/* 242 */     this.tabItemExceptions.setText("Exceptions (" + rule.exceptions.size() + ")");
/* 243 */     this.exceptionsList.removeAll();
/* 244 */     String s; for (localIterator = rule.getExceptionsString().iterator(); localIterator.hasNext(); this.exceptionsList.add(s)) s = (String)localIterator.next();
/*     */   }
/*     */   
/*     */   public static void openInEditor(IFile file, int startpos, int length)
/*     */   {
/* 249 */     org.eclipse.ui.IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
/* 250 */     HashMap map = new HashMap();
/* 251 */     map.put("charStart", new Integer(startpos));
/* 252 */     map.put("charEnd", new Integer(startpos + length));
/* 253 */     map.put("org.eclipse.ui.editorID", 
/* 254 */       "org.eclipse.ui.DefaultTextEditor");
/*     */     try {
/* 256 */       IMarker marker = file.createMarker("org.eclipse.core.resources.textmarker");
/* 257 */       marker.setAttributes(map);
/*     */       
/* 259 */       org.eclipse.ui.ide.IDE.openEditor(page, marker);
/*     */     } catch (Exception e) {
/* 261 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ }


/* Location:              /Users/UCLAPLSE/Downloads/LSclipse_1.0.4.jar!/lsclipse/views/RulesView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */