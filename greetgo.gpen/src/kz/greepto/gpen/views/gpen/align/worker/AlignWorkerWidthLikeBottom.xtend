package kz.greepto.gpen.views.gpen.align.worker

import kz.greepto.gpen.drawport.DrawPort
import kz.greepto.gpen.drawport.Kolor
import kz.greepto.gpen.util.ColorManager
import kz.greepto.gpen.drawport.Vec2

class AlignWorkerWidthLikeBottom implements AlignWorker {

  override paintIcon(DrawPort dp, ColorManager colors, int width, int height) {
    dp.style.foreground = Kolor.BLUE

    var t = Vec2.from(5, 5)

    dp.from(t + #[2, 50]).shift(40, -15).rect.draw

    dp.from(t + #[2, 35]).shift(0, -35).dashLine(0.1, 0.6, 5)
    dp.from(t + #[42, 35]).shift(0, -35).dashLine(0.1, 0.6, 5)

    var u = t + #[0, 0]

    dp.from(u + #[2, 16]).shift(45, -10).rect.draw
    dp.from(u + #[7, 30]).shift(35, -10).rect.draw

    dp.from(u + #[58, 11]).shift(-15, 0).line//
    .shift(2, -2).move.shift(-2, 2).line.shift(2, 2).line

    dp.from(u + #[15, 25]).shift(-12, 0).line//
    .shift(2, -2).move.shift(-2, 2).line.shift(2, 2).line

  }

}