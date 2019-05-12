import ru.tinkoff.service.params.PageParams

trait Common {

  def sliceCollection[T](pp: PageParams, col: Seq[T]): Seq[T] = {
    val skip = pp.page * pp.pageSize
    if (col.size > skip) col.drop(skip).take(math.min(pp.pageSize, col.size - skip))
    else Seq.empty
  }
}
