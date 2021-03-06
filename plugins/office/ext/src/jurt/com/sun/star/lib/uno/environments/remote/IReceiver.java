/*************************************************************************
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * Copyright 2008 by Sun Microsystems, Inc.
 *
 * OpenOffice.org - a multi-platform office productivity suite
 *
 * $RCSfile$
 * $Revision$
 *
 * This file is part of OpenOffice.org.
 *
 * OpenOffice.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * OpenOffice.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with OpenOffice.org.  If not, see
 * <http://www.openoffice.org/license.html>
 * for a copy of the LGPLv3 License.
 *
 ************************************************************************/

package com.sun.star.lib.uno.environments.remote;

/**
 * An abstraction for giving back a reply for a request.
 *
 * @version $Revision$ $ $Date$
 * @author Kay Ramme
 * @see com.sun.star.uno.IQueryInterface
 */
public interface IReceiver {
    /**
     * Send back a reply for a request.
     *
     * @param exception <CODE>true</CODE> if an exception (instead of a normal
     *     result) is sent back.
     * @param threadId the thread ID of the request.
     * @param result the result of executing the request, or an exception thrown
     *     while executing the request.
     */
    void sendReply(boolean exception, ThreadId threadId, Object result);
}
