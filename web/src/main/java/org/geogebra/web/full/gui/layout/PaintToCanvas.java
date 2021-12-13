package org.geogebra.web.full.gui.layout;

import elemental2.dom.CanvasRenderingContext2D;

public interface PaintToCanvas {
	void paintToCanvas(CanvasRenderingContext2D context2d, Runnable counter, int x, int y);
}
