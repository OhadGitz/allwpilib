/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008-2012. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary.tResourceType;
import edu.wpi.first.wpilibj.communication.UsageReporting;
import edu.wpi.first.wpilibj.hal.InterruptJNI;
import edu.wpi.first.wpilibj.hal.DIOJNI;
import edu.wpi.first.wpilibj.hal.InterruptJNI.InterruptHandlerFunction;
import edu.wpi.first.wpilibj.hal.HALUtil;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.util.AllocationException;
import edu.wpi.first.wpilibj.util.CheckedAllocationException;

/**
 * Class to read a digital input. This class will read digital inputs and return
 * the current value on the channel. Other devices such as encoders, gear tooth
 * sensors, etc. that are implemented elsewhere will automatically allocate
 * digital inputs and outputs as required. This class is only for devices like
 * switches etc. that aren't implemented anywhere else.
 */
public class DigitalInput extends DigitalSource implements LiveWindowSendable {

	/**
	 * Create an instance of a Digital Input class. Creates a digital input
	 * given a channel.
	 *
	 * @param channel
	 *            the port for the digital input
	 */
	public DigitalInput(int channel) {
        initDigitalPort(channel, true);

        UsageReporting.report(tResourceType.kResourceType_DigitalInput, channel);
	}

	/**
	 * Get the value from a digital input channel. Retrieve the value of a
	 * single digital input channel from the FPGA.
	 *
	 * @return the stats of the digital input
	 */
	public boolean get() {
		ByteBuffer status = ByteBuffer.allocateDirect(4);
		// set the byte order
		status.order(ByteOrder.LITTLE_ENDIAN);
		boolean value = DIOJNI.getDIO(m_port, status.asIntBuffer()) != 0;
		HALUtil.checkStatus(status.asIntBuffer());
		return value;
	}

	/**
	 * Get the channel of the digital input
	 *
	 * @return The GPIO channel number that this object represents.
	 */
	public int getChannel() {
		return m_channel;
	}

	public boolean getAnalogTriggerForRouting() {
		return false;
	}

	/**
	 * Request interrupts asynchronously on this digital input.
	 *
	 * @param handler
	 *            The address of the interrupt handler function of type
	 *            tInterruptHandler that will be called whenever there is an
	 *            interrupt on the digitial input port. Request interrupts in
	 *            synchronus mode where the user program interrupt handler will
	 *            be called when an interrupt occurs. The default is interrupt
	 *            on rising edges only.
	 */
	public void requestInterrupts(InterruptHandlerFunction handler) {
		// TODO: add interrupt support

		try {
			m_interruptIndex = interrupts.allocate();
		} catch (CheckedAllocationException e) {
			throw new AllocationException(
					"No interrupts are left to be allocated");
		}

		allocateInterrupts(false);

		ByteBuffer status = ByteBuffer.allocateDirect(4);
		// set the byte order
		status.order(ByteOrder.LITTLE_ENDIAN);
		InterruptJNI.requestInterrupts(m_interrupt, (byte) getModuleForRouting(),
				getChannelForRouting(),
				(byte) (getAnalogTriggerForRouting() ? 1 : 0), status.asIntBuffer());
		setUpSourceEdge(true, false);
		InterruptJNI.attachInterruptHandler(m_interrupt, handler, null, status.asIntBuffer());
		HALUtil.checkStatus(status.asIntBuffer());
	}

	/**
	 * Request interrupts synchronously on this digital input. Request
	 * interrupts in synchronus mode where the user program will have to
	 * explicitly wait for the interrupt to occur. The default is interrupt on
	 * rising edges only.
	 */
	public void requestInterrupts() {
		try {
			m_interruptIndex = interrupts.allocate();
		} catch (CheckedAllocationException e) {
			throw new AllocationException(
					"No interrupts are left to be allocated");
		}

		allocateInterrupts(true);

		ByteBuffer status = ByteBuffer.allocateDirect(4);
		// set the byte order
		status.order(ByteOrder.LITTLE_ENDIAN);
		InterruptJNI.requestInterrupts(m_interrupt, (byte) getModuleForRouting(),
				getChannelForRouting(),
				(byte) (getAnalogTriggerForRouting() ? 1 : 0), status.asIntBuffer());
		HALUtil.checkStatus(status.asIntBuffer());
		setUpSourceEdge(true, false);

	}

	/**
	 * Set which edge to trigger interrupts on
	 *
	 * @param risingEdge
	 *            true to interrupt on rising edge
	 * @param fallingEdge
	 *            true to interrupt on falling edge
	 */
	public void setUpSourceEdge(boolean risingEdge, boolean fallingEdge) {
		if (m_interrupt != null) {
			ByteBuffer status = ByteBuffer.allocateDirect(4);
			// set the byte order
			status.order(ByteOrder.LITTLE_ENDIAN);
			InterruptJNI.setInterruptUpSourceEdge(m_interrupt,
					(byte) (risingEdge ? 1 : 0), (byte) (fallingEdge ? 1 : 0),
					status.asIntBuffer());
			HALUtil.checkStatus(status.asIntBuffer());
		} else {
			throw new IllegalArgumentException(
					"You must call RequestInterrupts before setUpSourceEdge");
		}
	}

	/*
	 * Live Window code, only does anything if live window is activated.
	 */
	public String getSmartDashboardType() {
		return "Digital Input";
	}

	private ITable m_table;

	/**
	 * {@inheritDoc}
	 */
	public void initTable(ITable subtable) {
		m_table = subtable;
		updateTable();
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateTable() {
		if (m_table != null) {
			m_table.putBoolean("Value", get());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ITable getTable() {
		return m_table;
	}

	/**
	 * {@inheritDoc}
	 */
	public void startLiveWindowMode() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void stopLiveWindowMode() {
	}
}
