
package com.softhub.ps.image;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import com.softhub.ps.util.CharStream;
import java.io.IOException;

class CMYKMultipleSourceDecoder implements CMYKPixelSource {

	private ImageDecoder cyanComponent;
	private ImageDecoder magentaComponent;
	private ImageDecoder yellowComponent;
	private ImageDecoder blackComponent;

	CMYKMultipleSourceDecoder(
		ImageDataProducer producer, Object procs[/* 4 */], int bits
	) {
		cyanComponent = new ImageDecoder(producer, procs[0], bits);
		magentaComponent = new ImageDecoder(producer, procs[1], bits);
		yellowComponent = new ImageDecoder(producer, procs[2], bits);
		blackComponent = new ImageDecoder(producer, procs[3], bits);
	}

	CMYKMultipleSourceDecoder(CharStream data[/* 4 */], int bits) {
		cyanComponent = new ImageDecoder(data[0], bits);
		magentaComponent = new ImageDecoder(data[1], bits);
		yellowComponent = new ImageDecoder(data[2], bits);
		blackComponent = new ImageDecoder(data[3], bits);
	}

	public int nextCyanComponent() throws IOException {
		return cyanComponent.nextPixel();
	}

	public int nextMagentaComponent() throws IOException {
		return magentaComponent.nextPixel();
	}

	public int nextYellowComponent() throws IOException {
		return yellowComponent.nextPixel();
	}

	public int nextBlackComponent() throws IOException {
		return blackComponent.nextPixel();
	}

}
