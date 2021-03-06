/*
 * Copyright  2002,2004 The Apache Software Foundation
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
 *
 */
package org.apache.tools.ant.types.optional.image;

import javax.media.jai.PlanarImage;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/**
 *
 * @see org.apache.tools.ant.taskdefs.optional.image.Image
 */
public class Ellipse extends BasicShape implements DrawOperation {
    protected int width = 0;
    protected int height = 0;

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public PlanarImage executeDrawOperation() {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);

        Graphics2D graphics = (Graphics2D) bi.getGraphics();

        if (!stroke.equals("transparent")) {
            BasicStroke b_stroke = new BasicStroke(stroke_width);
            graphics.setColor(ColorMapper.getColorByName(stroke));
            graphics.setStroke(b_stroke);
            graphics.draw(new Ellipse2D.Double(0, 0, width, height));
        }

        if (!fill.equals("transparent")) {
            graphics.setColor(ColorMapper.getColorByName(fill));
            graphics.fill(new Ellipse2D.Double(0, 0, width, height));
        }


        for (int i = 0; i < instructions.size(); i++) {
            ImageOperation instr = ((ImageOperation) instructions.elementAt(i));
            if (instr instanceof DrawOperation) {
                PlanarImage img = ((DrawOperation) instr).executeDrawOperation();
                graphics.drawImage(img.getAsBufferedImage(), null, 0, 0);
            } else if (instr instanceof TransformOperation) {
                graphics = (Graphics2D) bi.getGraphics();
                PlanarImage image = ((TransformOperation) instr).executeTransformOperation(PlanarImage.wrapRenderedImage(bi));
                bi = image.getAsBufferedImage();
            }
        }
        return PlanarImage.wrapRenderedImage(bi);
    }
}
