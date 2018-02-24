/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pl.asie.charset.api.energy;

import java.util.ArrayList;

/**
 * Various general types of energy.
 *
 * Originally by neptunepink for the 1.8 version of Factorization.
 */
@SuppressWarnings("unused")
public final class EnergyCategory {
	public static final ArrayList<EnergyCategory> VALUES = new ArrayList<>();

	/**
	 * Mass moving in a straight line. Anvils fall; a camshaft drives a reciprocating follower.
	 */
	public static final EnergyCategory LINEAR = get("LINEAR");

	/**
	 * Mass rotating around a point, typically a center of mass. A waterwheel drives a shaft; gears invert the axis
	 * of rotation
	 */
	public static final EnergyCategory ROTATIONAL = get("ROTATIONAL");

	/**
	 * Energy held in substances that is released under certain conditions, often after some threshold of input energy
	 * is breached. Gunpowder can explode; steak can be digested. Uranium and hydrogen can undergo fission or fusion.
	 * Sand can hover in the air until a block update causes it to fall.
	 * <p/>
	 * Not really a unit of energy; more of a unit of storage. Usefully converting chemical energy between other
	 * forms tends to be slightly difficult.
	 */
	public static final EnergyCategory POTENTIAL = get("POTENTIAL");

	/**,k
	 * Positive pressure, such as from steam and compressed air.
	 * The atmosphere is a pressure source to a relative vacuum.
	 * Steam drives a piston. Hydrolic oil drives a piston.
	 */
	public static final EnergyCategory PRESSURE = get("PRESSURE");

	/**
	 * Sub-atomic particles moving at or near the speed of light.
	 * Photons, being light/electromagnetic radiation, are here.
	 * Also includes protons, electrons, etc.
	 */
	public static final EnergyCategory RADIATION = get("RADIATION");

	/**
	 * Heat. A byproduct, or sometimes direct product, of many reactions. The fire crackles. Magma rumbles deep
	 * in the nether.
	 */
	public static final EnergyCategory THERMAL = get("THERMAL");

	/**
	 * Electrons moving through, typically, metal wires. Alternating and Direct current. Also includes magnetism.
	 * Lightning strikes the ground. The magnet block pulls the door shut.
	 */
	public static final EnergyCategory ELECTRIC = get("ELECTRIC");

	/**
	 * Redstone signal. Strangely easy to create. Is fundamentally suppressive, but this suppression is often itself
	 * suppressed. Receiving a SIGNAL should probably be interpreted as a quick redstone pulse. Implementing this
	 * behavior is not at all obligatory, particularly in blocky contexts.
	 */
	public static final EnergyCategory SIGNAL = get("SIGNAL");

	/**
	 * Periodic motion along an elastic medium. Waves crash against rocks; two tectonic plates slide past
	 * one another, producing tremors; the noteblock plays a tone.
	 */
	public static final EnergyCategory OSCILLATION = get("OSCILLATION");

	/**
	 * Maybe it's sufficiently advanced technology. Maybe it's the eldritch.
	 */
	public static final EnergyCategory MAGIC = get("MAGIC");


	public final String name;
	private EnergyCategory(String name) {
		this.name = name;
	}

	public static EnergyCategory get(String name) {
		for (EnergyCategory cat : VALUES) {
			if (cat.name.equals(name)) {
				return cat;
			}
		}
		EnergyCategory ret = new EnergyCategory(name.intern());
		VALUES.add(ret);
		return ret;
	}
}
