package taxotests

class Book {
    String name
    static taxonomy = true

    static constraints = {
        name nullable: false, unique: true
    }
}
