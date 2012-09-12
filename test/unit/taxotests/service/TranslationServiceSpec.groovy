package taxotests.service

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*
import spock.lang.Unroll
import spock.lang.Specification

import static java.util.Locale.UK
import static java.util.Locale.FRANCE

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class TranslationServiceSpec extends Specification {
    private static Locale SPAIN = new Locale('es', 'ES')

    def translationService = new TranslationService()

    def setup() {
        translationService.init()
    }

    @Unroll("The translation of '#phrase' from #l1 to #l2 is '#translation'")
    def 'translates a phrase from and to various locales'() {
        expect:
        translation == translationService.translate(phrase, l1, l2)

        where:
        phrase  | l1    | l2     | translation
        'Hello' | UK    | FRANCE | 'Salut'
        'Hello' | UK    | SPAIN  | 'Hola'
        'World' | UK    | SPAIN  | 'Mundo'
        'Hola'  | SPAIN | UK     | 'Hello'
        'Hola'  | SPAIN | FRANCE | 'Salut'
    }

    def 'unknown translation returns null'() {
        expect:
        null == translationService.translate('Hooooo', UK, FRANCE)
    }
}
