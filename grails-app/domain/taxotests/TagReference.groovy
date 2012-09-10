package taxotests

class TagReference {
    static taxonomy = true
    String tag
    String locale

    static constraints = {
        tag nullable: false, unique: ['locale']
        locale nullable: false
    }

    Locale getLocaleObj() {
        new Locale(*locale.split('_'))
    }
}
