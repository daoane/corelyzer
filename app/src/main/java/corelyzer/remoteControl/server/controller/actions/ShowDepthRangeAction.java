/******************************************************************************
 *
 * CoreWall / Corelyzer - An Initial Core Description Tool
 * Copyright (C) 2008 Julian Yu-Chung Chen
 * Electronic Visualization Laboratory, University of Illinois at Chicago
 *
 * This software is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either Version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with this software; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Questions or comments about CoreWall should be directed to
 * cavern@evl.uic.edu
 *
 *****************************************************************************/
package corelyzer.remoteControl.server.controller.actions;

import corelyzer.controller.CRExperimentController;

public class ShowDepthRangeAction extends AbstractAction {
	public ShowDepthRangeAction(final String[] toks) {
		super(toks);
		this.setActionType(Type.VIEW);
	}

	public void run() {
		if (app != null) {
			float topDepth, bottomDepth;

			try {
				topDepth = Float.parseFloat(cmds[1].trim());
				bottomDepth = Float.parseFloat(cmds[2].trim());
			} catch (NumberFormatException e) {
				System.out.println("[ShowDepthRange] NaN depth: " + cmds[1] + ", " + cmds[2]);

				return;
			}

			CRExperimentController.showDepthRange(topDepth, bottomDepth);
		} else {
			System.out.println("---> [ShowDepthRange] from depth '" + cmds[1].trim() + "' to '" + cmds[2] + "' meters");
		}

	}
}
