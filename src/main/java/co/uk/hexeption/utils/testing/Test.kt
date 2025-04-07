/*

这是一个测试文件，你不需要改动其中的任何东西

*/
@file:Suppress("unused")

package co.uk.hexeption.utils.testing

import me.gb2022.quantum3d.vertex.ColorElement
import me.gb2022.quantum3d.vertex.DrawMode
import me.gb2022.quantum3d.vertex.VertexBuilderAllocator
import me.gb2022.quantum3d.vertex.VertexBuilderUploader
import me.gb2022.quantum3d.vertex.VertexFormat
import net.ccbluex.liquidbounce.utils.render.ColorUtils

class Test {
    fun example() {
        var allocator = VertexBuilderAllocator()

        var builder = allocator.allocate(
            VertexFormat.V3F_C4F, //vertex data format(depend on usage)
            DrawMode.QUADS, //opengl draw mode
            128 //max capacity of vertices
        )

        builder.setColor(
            1.0, 1.0, 1.0//defined as float so we use 0 -> 1 as [0,255] in RGB; _C4F as RGBA
        ).addVertex(114.0, 514.0, 1919.0)//vertex coordinate

        VertexBuilderUploader.uploadPointer(builder)//upload builder and parse all data to openGL.


        builder.reset()//clear all data so this builder can be re-used.
//看qq
        //or:

        allocator.free(builder)//free builder and release memory.

        //这个BYD为了提升性能（当然效果很好）用的是自定义堆外内存分配+直接地址访问
        //每个用完的对象必须free 否则堆外内存爆炸你系统都得裂开
        //这个SetColor不能直接用rgb(255,255,255,255)那你就给数据模式调成Byte被 或者 稍等下我去翻一个工具类

        //这样可以一股脑=把一大堆顶点数据按照单个draw-call直接丢给显卡 所以速度很快 非常快


        //颜色信息
        ColorElement.RGB(0x114514)//hex color like #FFFFFF
        ColorElement.RGB(255, 255, 255)
        var color3 = ColorElement.RGBA(0x114514FF)//hex color with RGBA


        color3.put(builder)//这样就可以转换了

        ColorElement.putInt1(builder,0x114514)//适用于不便于创建ColorElement的高性能场景
        ColorElement.putByte3(builder,255,255,255)//快速转换

    }//差不多了 我溜了

    fun byte3ToFloat3(r:Int,g:Int,b:Int): DoubleArray {
        var r2 = (r.and(255)) / 255.0
        var g2 = (g.and(255)) / 255.0
        var b2 = (b.and(255)) / 255.0
        return doubleArrayOf(r2, g2, b2)
    }

    fun int1ToFloat3(c: Int): DoubleArray {
        var r = (c.shr(16))
        var g = (c.shr(8))
        var b = c
        var r2 = (r.and(255)) / 255.0
        var g2 = (g.and(255)) / 255.0
        var b2 = (b.and(255)) / 255.0
        return doubleArrayOf(r2, g2, b2)
    }
}