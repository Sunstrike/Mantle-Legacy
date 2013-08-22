package io.sunstrike.mantle.algorithm

import scala.util.Random

import io.sunstrike.mantle.lib.Repo._

/*
 * FloatingIslandGen
 * io.sunstrike.mantle.algorithm
 *
 * Imported from io.sunstrike.corestrike.algorithms
 */

/**
 * Algorithm for generating conical-style floating islands.
 *
 * Caveat: Will only produce good quality islands if radius >= max_height
 *
 * @author Sunstrike
 */
class FloatingIslandGen(initSeed:Any = null) {

    //=== Variables/Construction ====================
    var seed:Any = initSeed
    var random:Random = null
    setSeed(initSeed)

    var grid:Array[Array[Array[Int]]] = null

    //=== Functions =================================
    /**
     * Sets the generators seed.
     *
     * @note This will RESET the current Random instance.
     * @note Use a String, long or no parameter. Using another type will be treated the same as null - A new randomised seed based on systime.
     * @param newSeed The new seed to use.
     */
    def setSeed(newSeed:Any = null) {
        seed = newSeed
        newSeed match {
            case s:String => random.setSeed(s.toLong)
            case l:Long => random.setSeed(l)
            case _ => random = new Random()
        }
    }

    /**
     * Generate one island.
     *
     * @note Verify that radius >= height, or poor results may be produced. Failing to check this also emits warnings in the console.
     *
     * @param maxHeight The islands max height.
     * @param maxTopRadius The islands max radius (but the final radius is usually slightly smaller)
     * @return The 3D grid in form [x][y][z], using Mathematical axises (x/y is the horizontal plane, z is height) NOT Minecraft axises.
     */
    def generate(maxHeight:Int, maxTopRadius:Int): Array[Array[Array[Int]]] = {
        if (maxHeight > maxTopRadius) {
            logger.warning("[FlIsGen] Max height is greater than max radius. This can result in poor quality islands.")
            logger.warning("[FlIsGen] Either ignore this message or add a radius >= height check in mod code.")
        }

        restartRandom
        grid = Array.fill(maxTopRadius)(Array.fill(maxTopRadius)(Array.fill(maxHeight)(0)))

        // Find and spawn centre seed block
        val centre:Int = math.round(maxTopRadius/2)
        grid(centre)(centre)(0) = 1

        // Calculate max splat size
        val maxSplat:Int = math.round(maxTopRadius/(maxHeight*2))
        //if (maxSplat <= 0) maxSplat = 1

        // Main loop
        for ( z <- 1 to maxHeight - 1 ) {
            for ( y <- 0 to maxTopRadius - 1 ) {
                for ( x <- 0 to maxTopRadius - 1 ) {
                    //logger.info("[FlIsGen] x: " + x + ", y: " + y + ", z: " + z + ", current grid: " + grid)
                    generateSplat(x, y, z, maxSplat)
                }
            }
        }

        grid
    }

    /**
     * Debug version of generate() call.
     *
     * This will emit warnings to the console to assist developers in removing debug code for release, displaying layer-by-layer
     * maps of the islands generated to console to verify whether it is mod code or the library producing issues.
     *
     * @param maxHeight The islands max height.
     * @param maxTopRadius The islands max radius (but the final radius is usually slightly smaller)
     * @return The 3D grid in form [x][y][z], using mathematical axes (x/y is the horizontal plane, z is height) NOT Minecraft axes.
     */
    def generateWithDebug(maxHeight:Int, maxTopRadius:Int): Array[Array[Array[Int]]] = {
        // Find the caller
        val stackTraceElements = Thread.currentThread().getStackTrace
        val callerTrace = stackTraceElements(2)
        val klass = callerTrace.getClassName
        val fname = callerTrace.getFileName
        val method = callerTrace.getMethodName
        val ln = callerTrace.getLineNumber
        // Warning
        logger.warning("[FlIsGen] Generating in debug mode. Will spam the console!")
        logger.warning("[FlIsGen] If this in production code, it is a major programming error; please report this to the developers!")
        logger.warning("[FlIsGen] Called from " + klass + " (" + fname + ":" + ln + ") in method " + method + ".")
        val g = generate(maxHeight, maxTopRadius)
        debugRenderGrid
        grid
    }

    private def debugRenderGrid {
        val vertical = grid(0)(0).length - 1
        logger.info("////////////START TRACE/////////////")
        for (z <- 0 to vertical) {
            logger.info("Layer " + (z+1))
            for (x <- grid) {
                var lnStr = ""
                for (y <- x) {
                    lnStr += y(z)
                }
                logger.info(lnStr)
            }
            logger.info("////////////LAYER BREAK////////////")
        }
        logger.info("////////////END TRACE///////////////")
    }

    private def restartRandom {
        seed match {
            case s:String => random.setSeed(s.toLong)
            case l:Long => random.setSeed(l)
            case _ => random = new Random()
        }
    }

    private def generateSplat(x:Int, y:Int, z:Int, maxSize:Int) {
        // Block underneath
        if (grid(x)(y)(z-1) == 0) return

        // Splat core
        grid(x)(y)(z) = 1

        var delta_x_pos = 0
        var delta_x_neg = 0
        var delta_y_pos = 0
        var delta_y_neg = 0

        for (_ <- 0 to maxSize*2) {
            val r = random.nextInt(4)
            //logger.info("[FlIsGen][generateSplat] x: " + x + ", y: " + y + ", z: " + z + ", maxSize: " + maxSize + ", dX+" + delta_x_pos + ", dX-" + delta_x_neg + ", dY+" + delta_y_pos + ", dY-" + delta_y_neg)
            r match {
                case 0 =>
                    // +x
                    delta_x_pos += 1
                    if (x+delta_x_pos < grid.length - 1)
                        grid(x+delta_x_pos)(y)(z) = 1
                case 1 =>
                    // +y
                    delta_y_pos += 1
                    if (y+delta_y_pos < grid(x).length - 1)
                        grid(x)(y+delta_y_pos)(z) = 1
                case 2 =>
                    // -x
                    delta_x_neg += 1
                    if (x-delta_x_neg > 0)
                        grid(x-delta_x_neg)(y)(z) = 1
                case 3 =>
                    // -y
                    delta_y_neg += 1
                    if (y-delta_y_neg > 0)
                        grid(x)(y-delta_y_neg)(z) = 1
                case _ =>
                    // Whut
                    val e = new IllegalStateException("Impossible value of r: " + r)
                    logger.warning("[FlIsGen] " + e.getMessage + " at: \n" + e.getStackTrace)
            }
        }
    }

}
