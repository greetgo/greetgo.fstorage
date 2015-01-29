package kz.greepto.gpen.views.gpen;

import java.util.ArrayList
import java.util.LinkedList
import java.util.List
import kz.greepto.gpen.editors.gpen.prop.PropAccessor
import kz.greepto.gpen.util.HandlerKiller
import org.eclipse.jface.viewers.ISelection
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Text
import org.eclipse.ui.ISelectionListener
import org.eclipse.ui.IWorkbenchPart
import org.eclipse.ui.part.ViewPart
import kz.greepto.gpen.editors.gpen.prop.SceneWorker
import kz.greepto.gpen.editors.gpen.GpenEditor
import kz.greepto.gpen.editors.gpen.prop.PropFactory
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.ui.views.contentoutline.ContentOutline
import kz.greepto.gpen.editors.gpen.outline.GpenContentOutlinePage

public class GpenPropertyView extends ViewPart {
  Composite parent
  Composite forFocus

  ISelectionListener listener
  val List<HandlerKiller> killers = new LinkedList

  def void killAll() {
    killers.forEach[kill]
    killers.clear
  }

  override void setFocus() {
    if(forFocus != null) forFocus.setFocus
  }

  SceneWorker sceneWorker = null

  override void createPartControl(Composite parent) {
    this.parent = parent

    listener = [ IWorkbenchPart part, ISelection selection |
      var ok = false
      if (part instanceof GpenEditor) {
        sceneWorker = (part as GpenEditor).sceneWorker
        ok = sceneWorker !== null
      }
      if (part instanceof ContentOutline) {
        var out = part as ContentOutline
        if (out.currentPage instanceof GpenContentOutlinePage) {
          var page = out.currentPage as GpenContentOutlinePage
          sceneWorker = page.sceneWorker
          ok = sceneWorker !== null
        }
      }
      if (ok && selection instanceof IStructuredSelection) {
        setSelection(selection as IStructuredSelection)
      } else {
        setSelection(null)
      }
    ]

    site.workbenchWindow.selectionService.addSelectionListener(listener)
  }

  override dispose() {
    if (listener != null) {
      site.workbenchWindow.selectionService.removeSelectionListener(listener)
      listener = null
    }
    killAll
    super.dispose()
  }

  def void setSelection(IStructuredSelection sel) {
    if(parent.disposed) return;
    parent.children.forEach[dispose]
    killAll

    if (sceneWorker === null || sel === null || sel.empty) {
      var lab = new Label(parent, SWT.NONE)
      lab.text = 'Выделите элементы в Gpen Editor-е'
      parent.layout(true)
      return;
    }

    var ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL + SWT.H_SCROLL)
    var Composite wall = new Composite(sc, SWT.NONE + SWT.BORDER)

    sc.content = wall
    sc.expandHorizontal = true
    sc.expandVertical = true

    var lay = new GridLayout
    lay.numColumns = 3
    wall.layout = lay

    val list = sel.iterator.map[sceneWorker.findByIdOrDie(it)].toList
    var propList = PropFactory.parseObjectList(list, sceneWorker)

    for (prop : propList) {
      appendPropWidgets(wall, prop)
    }

    {
      var lab = new Label(wall, SWT.NONE)
      lab.text = ''
    }
    new Label(wall, SWT.NONE).text = ''
    {
      val lab = new Label(wall, SWT.NONE)
      lab.text = '                                             '
    }

    sc.minSize = wall.computeSize(SWT.DEFAULT, SWT.DEFAULT)
    parent.layout(true)
  }

  def appendPropWidgets(Composite wall, PropAccessor prop) {
    if (prop.options.readonly) {
      appendReadonlyWidget(wall, prop)
      return
    }

    if (prop.type == Integer || prop.type == Integer.TYPE) {
      appendIntWidget(wall, prop)
      return
    }

    if (prop.type == String) {
      appendStrWidget(wall, prop)
      return
    }
    if (prop.type == Boolean || prop.type == Boolean.TYPE) {
      appendBoolWidget(wall, prop)
      return
    }
  }

  def appendReadonlyWidget(Composite wall, PropAccessor prop) {
    {
      var lab = new Label(wall, SWT.NONE)
      lab.text = prop.name
    }
    new Label(wall, SWT.NONE).text = ':'
    {
      val lab = new Label(wall, SWT.NONE)
      lab.text = extractStr(prop)
      killers += prop.addChangeHandler[lab.text = extractStr(prop)]
    }
  }

  def String extractStr(PropAccessor prop) {
    var value = prop.value
    if(value == null) return ''
    if (value instanceof Class<?>) {
      var klass = value as Class<?>
      return klass.simpleName
    }
    if (value instanceof String) {
      return value as String
    }
    return value.toString
  }

  def appendIntWidget(Composite wall, PropAccessor prop) {
    {
      var lab = new Label(wall, SWT.NONE)
      lab.text = prop.name
    }
    new Label(wall, SWT.NONE).text = ':'
    {
      val txt = new Text(wall, SWT.SINGLE + SWT.BORDER)
      var gd = new GridData()
      gd.horizontalAlignment = SWT.FILL
      txt.layoutData = gd
      val saved = new ArrayList<String>
      txt.text = extractStr(prop)
      saved += txt.text
      txt.addModifyListener [
        if (txt.text != saved.get(0)) {
          try {
            prop.value = Integer.valueOf(txt.text)
          } catch (NumberFormatException e) {
            prop.value = 0
          }
        }
      ]

      killers += prop.addChangeHandler [
        txt.text = extractStr(prop)
        saved.set(0, txt.text)
      ]
    }
  }

  def appendStrWidget(Composite wall, PropAccessor prop) {
    {
      var lab = new Label(wall, SWT.NONE)
      lab.text = prop.name
    }
    new Label(wall, SWT.NONE).text = ':'
    {
      val txt = new Text(wall, SWT.SINGLE + SWT.BORDER)
      var gd = new GridData()
      gd.horizontalAlignment = SWT.FILL
      txt.layoutData = gd
      val saved = new ArrayList<String>
      txt.text = extractStr(prop)
      saved += txt.text
      txt.addModifyListener [
        if (saved.get(0) != txt.text) {
          prop.value = txt.text
        }
      ]

      killers += prop.addChangeHandler [
        txt.text = extractStr(prop)
        saved.set(0, txt.text)
      ]
    }
  }

  def void appendBoolWidget(Composite wall, PropAccessor prop) {
    val btn = new Button(wall, SWT.CHECK)
    btn.text = prop.name
    var gd = new GridData()
    gd.horizontalSpan = 3
    gd.horizontalAlignment = SWT.FILL
    btn.layoutData = gd

    if (prop.value instanceof Boolean) {
      btn.grayed = false
      btn.selection = prop.value as Boolean
    } else {
      btn.selection = true
      btn.grayed = true
    }

    btn.addSelectionListener(
      new SelectionAdapter() {
        override widgetSelected(SelectionEvent e) {
          prop.value = btn.selection
          btn.grayed = false
        }
      })

    killers += prop.addChangeHandler[btn.selection = prop.value as Boolean]
  }

}