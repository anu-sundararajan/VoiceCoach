package com.nirmal.android.voicecoach;

/**
 * Performs an in-place complex FFT.
 *
 * Released under the MIT License
 *
 * Copyright (c) 2010 Gerald T. Beauregard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

import android.util.Log;

/**
 * Created by AXS43 on 2/6/2016.
 */
public class FFT {
    private static final String TAG = "Voice_FFT";
    private static final int LOGN = 11;  // Log2 FFT length

    private int m_logN = 0;     // log2 of FFT size
    private int m_N = 0;        // FFT size
    private double m_invN;      // Inverse of FFT length
    private int m_writePos = 0; // Position to write new audio from mic
    private double m_tempRe[];  // Temporary buffer - real part
    private double m_tempIm[];  // Temporary buffer - imaginary part
    private double m_mag[];     // Magnitudes (at each of the frequencies below)
    private double m_win[];     // Analysis window (Hanning)
    private double m_buf[];     // Buffer for mic audio

    private FFTElement[] m_X;

    public class FFTElement {
        public double re = 0;         // Real component
        public double im = 0;         // Imaginary component
        public int revTgt;           // Target position post bit-reversal

        public FFTElement(int r) {
            revTgt = r;
        }
    }

    /**
     * Initialize class to perform FFT of specified size.
     *
     * @param   logN    Log2 of FFT length. e.g. for 512 pt FFT, logN = 9.
     */
    public FFT()
    {
        m_logN = LOGN;
        m_N = 1 << m_logN;
        m_invN = 1.0/m_N;

        // Allocate elements for linked list of complex numbers.
        m_X = new FFTElement[m_N];
        m_tempRe = new double[m_N];
        m_tempIm = new double[m_N];
        m_mag = new double[m_N/2];
        m_win = new double[m_N];
        m_buf = new double[m_N];

        for (int k = 0; k < m_N; k++ ) {
            // Create element
            // Specify target for bit reversal re-ordering.
            m_X[k] = new FFTElement(BitReverse(k, m_logN));
            // Hanning analysis window
            m_win[k] = (4.0/m_N) * 0.5*(1-Math.cos(2*Math.PI*k/m_N));
            // Create a buffer for the input audio
            m_buf[k] = 0.0;
        }

    }

    /**
     * Performs in-place complex FFT.
     *
     * @param   xRe     Real part of input/output
     * @param   xIm     Imaginary part of input/output
     * @param   inverse If true (INVERSE), do an inverse FFT
     */
    public void runFFT()
    {
        int numFlies = m_N >> 1;    // Number of butterflies per sub-FFT
        int span = m_N >> 1;        // Width of the butterfly
        int spacing = m_N;          // Distance between start of sub-FFTs
        int wIndexStep = 1;         // Increment for twiddle table index
        int topIndex;
        int botIndex;

        // Copy data into linked complex number objects
        // If it's an IFFT, we divide by N while we're at it
        FFTElement x = m_X[0];
        int k = 0;
        double scale = 1;

        while (k < m_N)
        {
            x = m_X[k];
            x.re = scale*m_tempRe[k];
            x.im = scale*m_tempIm[k];
            k++;
        }

        // For each stage of the FFT
        for ( int stage = 0; stage < m_logN; ++stage )
        {
            // Compute a multiplier factor for the "twiddle factors".
            // The twiddle factors are complex unit vectors spaced at
            // regular angular intervals. The angle by which the twiddle
            // factor advances depends on the FFT stage. In many FFT
            // implementations the twiddle factors are cached, but because
            // vector lookup is relatively slow in ActionScript, it's just
            // as fast to compute them on the fly.
            double wAngleInc = wIndexStep * 2.0*Math.PI/m_N;
            //if (inverse == false) // Corrected 3 Aug 2011. Had this condition backwards before, so FFT was IFFT, and vice-versa!
                wAngleInc *= -1;
            double wMulRe = Math.cos(wAngleInc);
            double wMulIm = Math.sin(wAngleInc);

            for (int start = 0; start < m_N; start += spacing)
            {
                FFTElement xTop = m_X[start];
                FFTElement xBot = m_X[start+span];
                topIndex = start;
                botIndex = start + span;

                double wRe = 1.0;
                double wIm = 0.0;

                // For each butterfly in this stage
                for (int flyCount = 0; flyCount < numFlies; ++flyCount )
                {
                    // Get the top & bottom values
                    double xTopRe = xTop.re;
                    double xTopIm = xTop.im;
                    double xBotRe = xBot.re;
                    double xBotIm = xBot.im;

                    // Top branch of butterfly has addition
                    xTop.re = xTopRe + xBotRe;
                    xTop.im = xTopIm + xBotIm;

                    // Bottom branch of butterfly has subtraction,
                    // followed by multiplication by twiddle factor
                    xBotRe = xTopRe - xBotRe;
                    xBotIm = xTopIm - xBotIm;
                    xBot.re = xBotRe*wRe - xBotIm*wIm;
                    xBot.im = xBotRe*wIm + xBotIm*wRe;

                    // Advance butterfly to next top & bottom positions
                    topIndex++;
                    botIndex++;
                    if (topIndex < m_N) xTop = m_X[topIndex];
                    if (botIndex < m_N) xBot = m_X[botIndex];

                    // Update the twiddle factor, via complex multiply
                    // by unit vector with the appropriate angle
                    // (wRe + j wIm) = (wRe + j wIm) x (wMulRe + j wMulIm)
                    double tRe = wRe;
                    wRe = wRe*wMulRe - wIm*wMulIm;
                    wIm = tRe*wMulIm + wIm*wMulRe;
                }
            }

            numFlies >>= 1;   // Divide by 2 by right shift
            span >>= 1;
            spacing >>= 1;
            wIndexStep <<= 1;     // Multiply by 2 by left shift
        }

        // The algorithm leaves the result in a scrambled order.
        // Unscramble while copying values from the complex
        // linked list elements back to the input/output vectors.
        k = 0;
        while (k < m_N)
        {
            x = m_X[k];
            int target = x.revTgt;
            m_tempRe[target] = x.re;
            m_tempIm[target] = x.im;
            k++;
        }
    }

    public int calculateFrequency(int sampleRateInHz, short micData[]) {
        int i;
        // Get number of available input samples
        /******************************************************/
        /******************************************************/
        //int len = micData.length/4;
        int len = micData.length;
        /******************************************************/
        /******************************************************/
        int f = 0;
        // Read the input data and stuff it into the circular buffer
        for (i = 0; i < len; i++ )
        {
            // Convert the 16bit PCM data to float[-1,+1]
            //m_buf[m_writePos] = micData[i] / (float)32768;
            /******************************************************/
            /******************************************************/
            m_buf[m_writePos] = sinewave[f];
            f++;
            f = f % 40;
            /******************************************************/
            /******************************************************/
            //Log.i(TAG, " micData = " + micData[i]);
            if( m_buf[m_writePos] > 1 ) m_buf[m_writePos] = 1;
            if( m_buf[m_writePos] < -1 ) m_buf[m_writePos] = -1;
            m_writePos = (m_writePos+1)%m_N;
        }

        int pos = m_writePos;
        for (i = 0; i < m_N; i++ )
        {
            // Copy data from circular microphone audio buffer into temporary buffer for FFT,
            // while applying Hanning window.
            m_tempRe[i] = m_win[i]*m_buf[pos];
            pos = (pos+1)%m_N;
            //Log.i(TAG, " Han re = " + m_tempRe[i]);
            // Zero out the imaginary component
            m_tempIm[i] = 0.0;
        }

        final double SCALE = 20/Math.log(10); //LN10
        double max_mag = -Double.MAX_VALUE; //0; All the mags are negative so zero won't work.
        int bin_freq = sampleRateInHz/m_N;
        int freq = 0;

        // Do FFT and get magnitude spectrum
        //run(m_tempRe, m_tempIm, false);
        runFFT();

        for ( i = 0; i < m_N/2; i++ )
        {
            double re = m_tempRe[i];
            double im = m_tempIm[i];
            double prd = (re*re) + (im*im);
            double sqrt = Math.sqrt(prd);

            // Convert to dB magnitude
            // 20 log10(mag) => 20/ln(10) ln(mag)
            // Addition of MIN_VALUE prevents log from returning minus infinity if mag is zero
            // m_mag[i] = SCALE * Math.log10(sqrt + Double.MIN_VALUE);
            if (sqrt == 0) {
                m_mag[i] = SCALE * Math.log10(-Double.MAX_VALUE);
                Log.i(TAG, "sqrt = 0, i = " + i + " mag = " + m_mag[i]);
            }
            else m_mag[i] = SCALE * Math.log10(sqrt);

            //Log.i(TAG, "re = " + re + " im = " + im + " prd = " + prd + " sqrt = " + sqrt + " mag[i] = " + m_mag[i]);
            //Identify greatest magnitude possible fundamental Frequency
            if (max_mag < m_mag[i]) {
                max_mag = m_mag[i];
                Log.i(TAG, "max mag = " + m_mag[i]);
                // The FFT bins will correspond to frequencies that depend on the sample rate
                // and the length of the FFT. For example, if you use a sample rate of 22050
                // and a window with N=2048 (and logN=11), then the bin frequencies will be
                // multiples of 22050/2048 ~= 10.7 Hz. For real input, you can basically ignore
                // the output values above N/2, as they will contain the same info as the bins
                // below N/2 (just with the sign of the imaginary component flipped).
                freq = i * bin_freq;
                //Log.i(TAG, "freq = " + freq + " max_mag = " + max_mag);
            }
        }

        return freq;

    }

    /**
     * Do bit reversal of specified number of places of an int
     * For example, 1101 bit-reversed is 1011
     *
     * @param   x       Number to be bit-reverse.
     * @param   numBits Number of bits in the number.
     */
    private int BitReverse( int x, int numBits)
    {
        int y = 0;
        for ( int i = 0; i < numBits; i++)
        {
            y <<= 1;
            y |= x & 0x0001;
            x >>= 1;
        }
        return y;
    }

    private static final double[] sinewave = {
            0,
            0.156434581,
            0.309017218,
            0.453990813,
            0.587785632,
            0.707107196,
            0.809017408,
            0.891006897,
            0.951056806,
            0.987688506,
            1,
            0.987688139,
            0.951056081,
            0.891005832,
            0.809016029,
            0.707105537,
            0.587783734,
            0.453988723,
            0.309014986,
            0.156432263,
            -2.34641E-06,
            -0.156436898,
            -0.309019449,
            -0.453992904,
            -0.58778753,
            -0.707108855,
            -0.809018787,
            -0.891007962,
            -0.951057531,
            -0.987688873,
            -1,
            -0.987687772,
            -0.951055356,
            -0.891004767,
            -0.80901465,
            -0.707103878,
            -0.587781835,
            -0.453986632,
            -0.309012754,
            -0.156429946,
    };

}
