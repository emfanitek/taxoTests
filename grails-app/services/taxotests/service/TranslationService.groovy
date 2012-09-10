package taxotests.service

//TODO this is a stub implement with google translation API
class TranslationService {
    static transactional = false

    def x = [
        Hello: [
            es_ES: 'Hola',
            fr_FR: 'Salut'
        ],
        World: [
            es_ES: 'Mundo',
            fr_FR: 'Monde'
        ]
    ]

    String translate(String phrase, Locale srcLocale, Locale targetLocale) {
        assert srcLocale == Locale.UK
        x[phrase][targetLocale.toString()]
    }
}
