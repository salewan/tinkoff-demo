# ТЗ на scala-разработчика

Демо развёрнуто на heroku [https://pure-shore-41705.herokuapp.com](https://pure-shore-41705.herokuapp.com)

#### Для просмотра в сваггере

- зайти на http://petstore.swagger.io
- заменить *swagger.json* на `https://pure-shore-41705.herokuapp.com/api-docs/swagger.json`
- выполнять запросы к сервису можно через веб-интерфейс сваггера

#### Работать локально

- для запуска `sbt run`
- для запуска unit тестов `sbt test`

#### Использование утилиты httpie для удобства

- https://github.com/jakubroztocil/httpie/#installation

- примеры запросов  
  
    локально  
    `http GET 127.0.0.1:8080/books`  
    `http GET 127.0.0.1:8080/authors pageSize==3`  
    `http GET 127.0.0.1:8080/books/withSortByViews pageSize==5 page==100 order==desc`  
      
    или то же самое на heroku  
    `http GET https://pure-shore-41705.herokuapp.com/books`  
    `http GET https://pure-shore-41705.herokuapp.com/authors pageSize==3`  
    `http GET https://pure-shore-41705.herokuapp.com/books/withSortByViews pageSize==5 page==100 order==desc`
    