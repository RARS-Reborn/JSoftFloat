package com.github.unaimillan.jsoftfloat;

import com.github.unaimillan.jsoftfloat.internal.ExactFloat;
import com.github.unaimillan.jsoftfloat.operations.Conversions;
import com.github.unaimillan.jsoftfloat.types.Float32;
import com.github.unaimillan.jsoftfloat.types.Float64;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TestConversions {
    @Test
    public void Float32fromInteger() {
        assertEquals(0x0, Float32.fromInteger(0).bits);
        assertEquals(0x3f800000, Float32.fromInteger(1).bits);
        assertEquals(0x40000000, Float32.fromInteger(2).bits);
        assertEquals(0xbf800000, Float32.fromInteger(-1).bits);
        assertEquals(0x44000000, Float32.fromInteger(512).bits);
        assertEquals(0x4e5693a4, Float32.fromInteger(900000000).bits);
        // TODO: boundary size test
    }

    @Test
    public void IntegerfromFloat32() {
        assertEquals(0, Conversions.convertToInt(Float32.Zero, new Environment(),true));
        assertEquals(1, Conversions.convertToInt(Float32.fromInteger(1), new Environment(),true));
        assertEquals(-1, Conversions.convertToInt(Float32.fromInteger(-1), new Environment(),true));


        for(int i = 1; i != 0; i <<= 1){
            assertEquals(i, roundTripInt(i));
            assertEquals(i, roundTripUnsignedInt(i));
        }
        for(long i = 1; i != 0; i <<= 1){
            assertEquals(i, roundTripLong(i));
            assertEquals(i, roundTripUnsignedLong(i));
        }
    }

    public int roundTripInt(int a){
        Float32 tmp = Conversions.convertFromInt(BigInteger.valueOf(a),new Environment(),new Float32(0));
        return Conversions.convertToInt(tmp,new Environment(),true);
    }
    public int roundTripInt64(int a){
        Float64 tmp = Conversions.convertFromInt(BigInteger.valueOf(a),new Environment(),new Float64(0));
        return Conversions.convertToInt(tmp,new Environment(),true);
    }
    public int roundTripUnsignedInt(int a){
        BigInteger unsigned = BigInteger.valueOf(a);
        if(a < 0){
            unsigned = unsigned.add(BigInteger.ONE.shiftLeft(32));
        }
        Float32 tmp = Conversions.convertFromInt(unsigned,new Environment(),new Float32(0));
        return Conversions.convertToUnsignedInt(tmp,new Environment(),true);
    }
    public long roundTripLong(long a){
        Float32 tmp = Conversions.convertFromInt(BigInteger.valueOf(a),new Environment(),new Float32(0));
        return Conversions.convertToLong(tmp,new Environment(),true);
    }
    public long roundTripUnsignedLong(long a){
        BigInteger unsigned = BigInteger.valueOf(a);
        if(a < 0){
            unsigned = unsigned.add(BigInteger.ONE.shiftLeft(64));
        }
        Float32 tmp = Conversions.convertFromInt(unsigned,new Environment(),new Float32(0));
        return Conversions.convertToUnsignedLong(tmp,new Environment(),true);
    }


    @Test
    public void UnsignedConversions() {
        assertEquals(0, Conversions.convertToInt(Float32.Zero, new Environment(),true));
        assertEquals(1, Conversions.convertToInt(Float32.fromInteger(1), new Environment(),true));
        assertEquals(-1, Conversions.convertToInt(Float32.fromInteger(-1), new Environment(),true));
        Environment env = new Environment();
        Float32 tmp = Conversions.convertFromInt(BigInteger.valueOf(0x80000000L),env,new Float32(0));
        int signed = Conversions.convertToInt(tmp,env,true), unsigned = Conversions.convertToUnsignedInt(tmp,env,true);
        assertNotEquals(signed,unsigned);
        assertEquals(Integer.MAX_VALUE,signed);
        assertEquals(0x80000000,unsigned);
    }

    @Test
    void SubnormalConversion() {
        ExactFloat small = new ExactFloat(false, -150, BigInteger.valueOf(3));
        assertEquals(0x00000001, Float32.fromExact(small, new Environment(RoundingMode.zero)).bits);
        assertEquals(0x00000001, Float32.fromExact(small, new Environment(RoundingMode.min)).bits);
        assertEquals(0x00000002, Float32.fromExact(small, new Environment(RoundingMode.even)).bits);
        assertEquals(0x00000002, Float32.fromExact(small, new Environment(RoundingMode.max)).bits);
        assertEquals(0x00000002, Float32.fromExact(small, new Environment(RoundingMode.away)).bits);

        // Test below smallest subnormal
        small = new ExactFloat(false, -150, BigInteger.ONE); // Perfectly in the middle
        assertEquals(0x00000000, Float32.fromExact(small, new Environment(RoundingMode.zero)).bits);
        assertEquals(0x00000000, Float32.fromExact(small, new Environment(RoundingMode.min)).bits);
        assertEquals(0x00000000, Float32.fromExact(small, new Environment(RoundingMode.even)).bits);
        assertEquals(0x00000001, Float32.fromExact(small, new Environment(RoundingMode.max)).bits);
        assertEquals(0x00000001, Float32.fromExact(small, new Environment(RoundingMode.away)).bits);
        small = new ExactFloat(false, -151, BigInteger.valueOf(3)); // Biased away from 0
        assertEquals(0x00000000, Float32.fromExact(small, new Environment(RoundingMode.zero)).bits);
        assertEquals(0x00000000, Float32.fromExact(small, new Environment(RoundingMode.min)).bits);
        assertEquals(0x00000001, Float32.fromExact(small, new Environment(RoundingMode.even)).bits);
        assertEquals(0x00000001, Float32.fromExact(small, new Environment(RoundingMode.max)).bits);
        assertEquals(0x00000001, Float32.fromExact(small, new Environment(RoundingMode.away)).bits);
        small = new ExactFloat(false, -151, BigInteger.ONE); // Biased towards 0
        assertEquals(0x00000000, Float32.fromExact(small, new Environment(RoundingMode.zero)).bits);
        assertEquals(0x00000000, Float32.fromExact(small, new Environment(RoundingMode.min)).bits);
        assertEquals(0x00000000, Float32.fromExact(small, new Environment(RoundingMode.even)).bits);
        assertEquals(0x00000001, Float32.fromExact(small, new Environment(RoundingMode.max)).bits);
        assertEquals(0x00000000, Float32.fromExact(small, new Environment(RoundingMode.away)).bits);

        small = new ExactFloat(false, -150, BigInteger.valueOf(5));
        assertEquals(0x00000002, Float32.fromExact(small, new Environment(RoundingMode.even)).bits);


        small = new ExactFloat(false, -151, BigInteger.valueOf(5));
        assertEquals(0x00000001, Float32.fromExact(small, new Environment(RoundingMode.even)).bits);
        assertEquals(0x00000001, Float32.fromExact(small, new Environment(RoundingMode.away)).bits);

        small = new ExactFloat(false, -151, BigInteger.valueOf(7));
        assertEquals(0x00000002, Float32.fromExact(small, new Environment(RoundingMode.even)).bits);
        assertEquals(0x00000002, Float32.fromExact(small, new Environment(RoundingMode.away)).bits);


        ExactFloat largeSubnormal = new Float32(0x007FFFFF).toExactFloat();
        ExactFloat belowSubnormal = new ExactFloat(false, -150, BigInteger.ONE);
        ExactFloat normal = largeSubnormal.add(belowSubnormal);

        assertEquals(0x007FFFFF, Float32.fromExact(normal, new Environment(RoundingMode.zero)).bits);
        assertEquals(0x007FFFFF, Float32.fromExact(normal, new Environment(RoundingMode.min)).bits);
        assertEquals(0x00800000, Float32.fromExact(normal, new Environment(RoundingMode.even)).bits);
        assertEquals(0x00800000, Float32.fromExact(normal, new Environment(RoundingMode.max)).bits);
        assertEquals(0x00800000, Float32.fromExact(normal, new Environment(RoundingMode.away)).bits);
    }

    @Test
    void BasicConversions() {
        assertEquals(0x00000001, Float32.fromExact(new Float32(0x00000001).toExactFloat(), new Environment()).bits);
        assertEquals(0x00000010, Float32.fromExact(new Float32(0x00000010).toExactFloat(), new Environment()).bits);
        assertEquals(0x00400000, Float32.fromExact(new Float32(0x00400000).toExactFloat(), new Environment()).bits);
        assertEquals(0x7F7FFFFF, Float32.fromExact(new Float32(0x7F7FFFFF).toExactFloat(), new Environment()).bits);

        Environment e = new Environment();
        assertEquals(0x00000000, Float32.fromExact(new ExactFloat(false, -151, BigInteger.ONE), e).bits);
        assertTrue(e.flags.contains(Flags.inexact));
        assertTrue(e.flags.contains(Flags.underflow));
    }

    @Test
    void NormalRoundingConversions() {
        ExactFloat ef = new ExactFloat(false, 0, BigInteger.ONE);
        ExactFloat small = new ExactFloat(false, -24, BigInteger.ONE);

        assertEquals(0x3F800000, Float32.fromExact(ef.add(small), new Environment(RoundingMode.zero)).bits);
        assertEquals(0x3F800000, Float32.fromExact(ef.add(small), new Environment(RoundingMode.min)).bits);
        assertEquals(0x3F800000, Float32.fromExact(ef.add(small), new Environment(RoundingMode.even)).bits);
        assertEquals(0x3F800001, Float32.fromExact(ef.add(small), new Environment(RoundingMode.max)).bits);
        assertEquals(0x3F800001, Float32.fromExact(ef.add(small), new Environment(RoundingMode.away)).bits);

        small = new ExactFloat(false, -25, BigInteger.valueOf(3));
        assertEquals(0x3F800001, Float32.fromExact(ef.add(small), new Environment(RoundingMode.even)).bits);

        small = new ExactFloat(false, -25, BigInteger.ONE);
        assertEquals(0x3F800000, Float32.fromExact(ef.add(small), new Environment(RoundingMode.away)).bits);

        ef = new Float32(0x3FFFFFFF).toExactFloat();
        small = new ExactFloat(false, -24, BigInteger.ONE);

        assertEquals(0x3FFFFFFF, Float32.fromExact(ef.add(small), new Environment(RoundingMode.zero)).bits);
        assertEquals(0x3FFFFFFF, Float32.fromExact(ef.add(small), new Environment(RoundingMode.min)).bits);
        assertEquals(0x40000000, Float32.fromExact(ef.add(small), new Environment(RoundingMode.even)).bits);
        assertEquals(0x40000000, Float32.fromExact(ef.add(small), new Environment(RoundingMode.max)).bits);
        assertEquals(0x40000000, Float32.fromExact(ef.add(small), new Environment(RoundingMode.away)).bits);
    }

    @Test
    void LargeConversions() {
        Float32 max = new Float32(0x7F7FFFFF);
        ExactFloat tooBig = new ExactFloat(false, 128, BigInteger.ONE);
        Environment e = new Environment(RoundingMode.zero);
        assertEquals(max.bits, Float32.fromExact(tooBig, e).bits);
        assertTrue(e.flags.contains(Flags.inexact));
        assertTrue(e.flags.contains(Flags.overflow));
        assertEquals(max.bits, Float32.fromExact(tooBig, new Environment(RoundingMode.min)).bits);
        assertEquals(Float32.Infinity.bits, Float32.fromExact(tooBig, new Environment(RoundingMode.max)).bits);
        assertEquals(Float32.Infinity.bits, Float32.fromExact(tooBig, new Environment(RoundingMode.even)).bits);
        assertEquals(Float32.Infinity.bits, Float32.fromExact(tooBig, new Environment(RoundingMode.away)).bits);

        max = max.negate();
        tooBig = tooBig.negate();
        assertEquals(max.bits, Float32.fromExact(tooBig, e).bits);
        assertEquals(max.bits, Float32.fromExact(tooBig, new Environment(RoundingMode.max)).bits);
        assertEquals(Float32.NegativeInfinity.bits, Float32.fromExact(tooBig, new Environment(RoundingMode.min)).bits);
        assertEquals(Float32.NegativeInfinity.bits, Float32.fromExact(tooBig, new Environment(RoundingMode.even)).bits);
        assertEquals(Float32.NegativeInfinity.bits, Float32.fromExact(tooBig, new Environment(RoundingMode.away)).bits);
    }

    @Test
    void RoundToIntegral() {
        assertEquals(Float32.Zero.bits, Conversions.roundToIntegral(Float32.Zero, new Environment()).bits);
        assertEquals(Float32.NegativeZero.bits, Conversions.roundToIntegral(Float32.NegativeZero, new Environment()).bits);

        // 0x3F000000 = 0.5, 0x3F800000 = 1
        Float32 half = new Float32(0x3F000000);
        assertEquals(Float32.Zero.bits, Conversions.roundToIntegral(half, new Environment(RoundingMode.min)).bits);
        assertEquals(Float32.Zero.bits, Conversions.roundToIntegral(half, new Environment(RoundingMode.zero)).bits);
        assertEquals(Float32.Zero.bits, Conversions.roundToIntegral(half, new Environment(RoundingMode.even)).bits);
        assertEquals(0x3F800000, Conversions.roundToIntegral(half, new Environment(RoundingMode.away)).bits);
        assertEquals(0x3F800000, Conversions.roundToIntegral(half, new Environment(RoundingMode.max)).bits);

        half = half.negate();
        assertEquals(0xBF800000, Conversions.roundToIntegral(half, new Environment(RoundingMode.min)).bits);
        assertEquals(Float32.NegativeZero.bits, Conversions.roundToIntegral(half, new Environment(RoundingMode.zero)).bits);
        assertEquals(Float32.NegativeZero.bits, Conversions.roundToIntegral(half, new Environment(RoundingMode.even)).bits);
        assertEquals(0xBF800000, Conversions.roundToIntegral(half, new Environment(RoundingMode.away)).bits);
        assertEquals(Float32.NegativeZero.bits, Conversions.roundToIntegral(half, new Environment(RoundingMode.max)).bits);

        Float32 threeHalfs = new Float32(0x3F400000);
        assertEquals(0x3F800000, Conversions.roundToIntegral(threeHalfs, new Environment(RoundingMode.even)).bits);
        assertEquals(0x3F800000, Conversions.roundToIntegral(threeHalfs, new Environment(RoundingMode.away)).bits);

        Float32 oneFourth = new Float32(0x3EB00000);
        assertEquals(Float32.Zero.bits, Conversions.roundToIntegral(oneFourth, new Environment(RoundingMode.even)).bits);
        assertEquals(Float32.Zero.bits, Conversions.roundToIntegral(oneFourth, new Environment(RoundingMode.away)).bits);
    }
}
