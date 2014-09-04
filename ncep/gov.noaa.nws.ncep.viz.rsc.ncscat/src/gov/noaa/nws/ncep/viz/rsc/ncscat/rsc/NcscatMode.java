/**
 * 
 */
package gov.noaa.nws.ncep.viz.rsc.ncscat.rsc;

import java.nio.ByteOrder;

/**
 * NcscatMode - Enum class to centralize and encapsulate all the things that
 * vary among different satellite and data feed types.
 * 
 * //TODO: Consider moving this information entirely to the bundle and/or
 * preferences (.xml/.prm) files, so the Java code can be completely agnostic
 * about satellite data types; would allow extended 'configurability', at the
 * expense of slightly longer bundle/preference files...
 * 
 * This code has been developed by the SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 02 Jun 2010  235B       B. Hebbard  Initial creation.
 * 03 Feb 2011  235E       B. Hebbard  Add support for ambiguity variants.
 * 16 Aug 2012             B. Hebbard  Add OSCAT / OSCAT_HI
 * 11 Apr 2014  1128       B. Hebbard  Add longitudeCoding field; change wind sense from boolean to enum.
 * 
 * </pre>
 * 
 * @author bhebbard
 * @version 1.0
 */

public enum NcscatMode {

    // @formatter:off
        QUIKSCAT        (  76, WindDirectionSense.METEOROLOGICAL, LongitudeCoding.UNSIGNED, ByteOrder.BIG_ENDIAN ),
        QUIKSCAT_HI     ( 152, WindDirectionSense.METEOROLOGICAL, LongitudeCoding.UNSIGNED, ByteOrder.BIG_ENDIAN ),
        ASCAT           (  42, WindDirectionSense.OCEANOGRAPHIC,  LongitudeCoding.UNSIGNED, ByteOrder.BIG_ENDIAN ),
        ASCAT_HI        (  82, WindDirectionSense.OCEANOGRAPHIC,  LongitudeCoding.UNSIGNED, ByteOrder.BIG_ENDIAN ),
        EXASCT          (  42, WindDirectionSense.OCEANOGRAPHIC,  LongitudeCoding.UNSIGNED, ByteOrder.LITTLE_ENDIAN ),
        EXASCT_HI       (  82, WindDirectionSense.OCEANOGRAPHIC,  LongitudeCoding.UNSIGNED, ByteOrder.LITTLE_ENDIAN ),
        OSCAT           (  36, WindDirectionSense.METEOROLOGICAL, LongitudeCoding.UNSIGNED, ByteOrder.BIG_ENDIAN ),
        OSCAT_HI        (  76, WindDirectionSense.METEOROLOGICAL, LongitudeCoding.UNSIGNED, ByteOrder.LITTLE_ENDIAN ),
        WSCAT           (  79, WindDirectionSense.METEOROLOGICAL, LongitudeCoding.SIGNED,   ByteOrder.LITTLE_ENDIAN ),
        UNKNOWN         (  76, WindDirectionSense.METEOROLOGICAL, LongitudeCoding.UNSIGNED, ByteOrder.BIG_ENDIAN );
    // @formatter:on

    private int pointsPerRow; // number of Wind Vector Cell in each scan row
                              // across satellite track

    private WindDirectionSense windDirectionSense; // is the numeric wind
                                                   // direction the "from"
                                                   // (METEOROLOGICAL)
                                                   // direction, or the "to"
                                                   // (OCEANOGRAPHIC) direction?

    private LongitudeCoding longitudeCoding; // is the two-byte value a SIGNED
                                             // (-18000 -- +18000) or UNSIGNED
                                             // (0 -- 36000) representation of
                                             // the (scaled by 100) longitude
                                             // east of Greenwich?

    private ByteOrder byteOrder; // endianess of data in the byte stream

    // TODO: could add more here, to simplify (switch) code with more table
    // driven logic. But see above note about .xml/.prm...

    // Constructor
    NcscatMode(int pointsPerRow, WindDirectionSense windDirectionSense,
            LongitudeCoding longitudeCoding, ByteOrder byteOrder) {
        this.pointsPerRow = pointsPerRow;
        this.windDirectionSense = windDirectionSense;
        this.longitudeCoding = longitudeCoding;
        this.byteOrder = byteOrder;
    }

    public int getPointsPerRow() {
        return pointsPerRow;
    }

    public WindDirectionSense getWindDirectionSense() {
        return windDirectionSense;
    }

    public LongitudeCoding getLongitudeCoding() {
        return longitudeCoding;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public static NcscatMode stringToMode(String name) {
        // Given a string, return the corresponding enum
        NcscatMode returnValue = null;
        name = name.toUpperCase();
        name = name.replaceAll("-", "_");
        // TODO: Remove ambiguity number??
        try {
            returnValue = valueOf(name);
        } catch (IllegalArgumentException e) {
            // TODO: Signal unrecognized Ncscat mode string
            returnValue = UNKNOWN;
        }
        return returnValue;
    }

    public enum WindDirectionSense { // numeric direction value gives...
        METEOROLOGICAL, // degrees FROM which wind is blowing
        OCEANOGRAPHIC // degrees TO which wind is blowing
    }

    public enum LongitudeCoding { // 2-byte wvc_lon (Wind Vector Cell -
                                  // longitude) field is (x0.01 deg)...
        SIGNED, // SIGNED short (-18000..+18000)
        UNSIGNED // UNSIGNED short (0..36000 east of Greenwich)
    }

}
