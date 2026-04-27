// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FocusVisualizerTest {
    var testSubject: FocusVisualizer? = null

    @Mock
    var focusVisualizerStylesMock: FocusVisualizerStyles? = null

    @Mock
    var focusVisualizationCanvasMock: FocusVisualizationCanvas? = null

    @Mock
    var accessibilityEventMock: AccessibilityNodeInfo? = null

    var focusElementHighlightConstructionMock: MockedConstruction<FocusElementHighlight?>? = null
    var focusElementLineConstructionMock: MockedConstruction<FocusElementLine?>? = null

    @Before
    @Throws(Exception::class)
    fun prepare() {
        focusElementHighlightConstructionMock =
            Mockito.mockConstruction<FocusElementHighlight?>(FocusElementHighlight::class.java)
        focusElementLineConstructionMock =
            Mockito.mockConstruction<FocusElementLine?>(FocusElementLine::class.java)

        testSubject = FocusVisualizer(focusVisualizerStylesMock!!, focusVisualizationCanvasMock!!)
    }

    @After
    @Throws(Exception::class)
    fun cleanUp() {
        focusElementLineConstructionMock!!.close()
        focusElementHighlightConstructionMock!!.close()
    }

    @Test
    fun returnsNotNull() {
        Assert.assertNotNull(testSubject)
    }

    /* TODO: fix Whitebox cases

  @Test
  public void addNewFocusedElementCreatesElementOnFirstCall() {
    testSubject.addNewFocusedElement(accessibilityEventMock);
    ArrayList<FocusElementHighlight> resultingHighlightList =
        Whitebox.getInternalState(testSubject, "focusElementHighlights");
    Assert.assertEquals(resultingHighlightList.size(), 1);
  }

  @Test
  public void addNewFocusedElementCreatesLineOnFirstCall() {
    testSubject.addNewFocusedElement(accessibilityEventMock);
    ArrayList<FocusElementLine> resultingLineList =
        Whitebox.getInternalState(testSubject, "focusElementLines");
    Assert.assertEquals(resultingLineList.size(), 1);
  }

  @Test
  public void secondAccessibilityEventSetsPreviousElementNonCurrent() throws Exception {
    FocusVisualizer testSubjectSpy = spy(testSubject);
    testSubjectSpy.addNewFocusedElement(accessibilityEventMock);
    testSubjectSpy.addNewFocusedElement(accessibilityEventMock);

    verifyPrivate(testSubjectSpy, times(1))
        .invoke("setPreviousElementHighlightNonCurrent", any(FocusElementHighlight.class));
  }

  @Test
  public void secondAccessibilityEventSetsPreviousLineNonCurrent() throws Exception {
    FocusVisualizer testSubjectSpy = spy(testSubject);
    testSubjectSpy.addNewFocusedElement(accessibilityEventMock);
    testSubjectSpy.addNewFocusedElement(accessibilityEventMock);

    verifyPrivate(testSubjectSpy, times(1))
        .invoke("setPreviousLineNonCurrent", any(FocusElementLine.class));
  }

  @Test
  public void tabStopCountIncrementsAsExpected() {
    testSubject.addNewFocusedElement(accessibilityEventMock);
    testSubject.addNewFocusedElement(accessibilityEventMock);
    testSubject.addNewFocusedElement(accessibilityEventMock);

    int resultingTabStopCount = Whitebox.getInternalState(testSubject, "tabStopCount");

    Assert.assertEquals(resultingTabStopCount, 3);
  }

  @Test
  public void resetVisualizationsDoesTheJob() {
    testSubject.addNewFocusedElement(accessibilityEventMock);
    testSubject.addNewFocusedElement(accessibilityEventMock);

    testSubject.resetVisualizations();
    ArrayList<FocusElementLine> resultingLineList =
        Whitebox.getInternalState(testSubject, "focusElementLines");
    ArrayList<FocusElementHighlight> resultingHighlightList =
        Whitebox.getInternalState(testSubject, "focusElementHighlights");
    int resultingTabStopCount = Whitebox.getInternalState(testSubject, "tabStopCount");

    Assert.assertEquals(resultingHighlightList.size(), 0);
    Assert.assertEquals(resultingLineList.size(), 0);
    Assert.assertEquals(resultingTabStopCount, 0);
  }

     */
    @Test
    fun refreshHighlightsCallsRedraw() {
        testSubject!!.refreshHighlights()
        Mockito.verify<FocusVisualizationCanvas?>(focusVisualizationCanvasMock).redraw()
    }
}
