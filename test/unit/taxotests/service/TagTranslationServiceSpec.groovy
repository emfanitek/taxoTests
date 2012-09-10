package taxotests.service

import com.grailsrocks.taxonomy.Taxon
import com.grailsrocks.taxonomy.TaxonLink
import com.grailsrocks.taxonomy.Taxonomy
import com.grailsrocks.taxonomy.TaxonomyService
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import taxotests.Book
import taxotests.TagReference

import static java.util.Locale.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Book, TagReference, Taxon, Taxonomy, TaxonLink])
class TagTranslationServiceSpec extends Specification {
    def b1

    final static String T1_GERMAN = 'Hallo'
    final static String T1_UK = 'Hello'
    final static String T2_UK = 'World'
    final static String T1_FR = 'Salut'
    final static String T2_FR = 'Monde'
    final static String T1_ES = 'Hola'
    final static String T2_ES = 'Mundo'
    final static Locale SPAIN = new Locale('es', 'ES')

    def taxonomyService = new TaxonomyService()
    def taggingService = new TaggingService()
    def taxonomies = new TaxonomyHelper(taxonomyService)

    def tagTranslationService = new TagTranslationService(
        availableLocales: [FRANCE, SPAIN, UK],
        translationService: new TranslationService(),
        taxonomyService: taxonomyService
    )

    void setup() {
        taxonomies.instrumentTaxonomyMethods([Book, TagReference])

        taxonomyService.init()
        b1 = new Book(name: 'b1').save()
    }

    private Set allTags(obj) {
        obj.taxonomies.collect {it.name} as Set
    }

    private boolean setsAreEqual(Collection expected, Collection actual) {
        actual as Set == expected as Set
    }

    def 'translates a tag to available locales and adds the translation to the object'() {
        setup:
        def expected = [T1_UK, T1_ES, T1_FR] as Set
        taggingService.tag(b1, UK, T1_UK)

        when:
        tagTranslationService.translateTag(b1, UK, T1_UK)
        def ref = TagReference.findByLocaleAndTag(UK.toString(), T1_UK)

        then:
        allTags(ref) == expected
        allTags(b1) == expected
    }

    def 'translates all tags to available locales'() {
        setup:
        taggingService.tag(b1, UK, T1_UK)
        taggingService.tag(b1, UK, T2_UK)

        when:
        tagTranslationService.translateAllTags(b1)

        then:
        setsAreEqual([
            T1_UK, T1_ES, T1_FR,
            T2_UK, T2_ES, T2_FR
        ],
            allTags(b1))
    }

    def 'translation attempt from unknown locale fails'() {
        setup:
        taggingService.tag(b1, GERMANY, T1_GERMAN)

        when:
        tagTranslationService.translateTag(b1, GERMANY, T1_GERMAN)

        then:
        thrown(AssertionError)
    }

    def 'finds an object by its given tag'() {
        setup:
        def b2 = new Book(name: 'b2').save()
        taggingService.tag(b1, locale, tag1)
        taggingService.tag(b2, locale, tag2)
        tagTranslationService.translateTag(b2, locale, tag2)


        when:
        List allBooksWithT1 = Book.findAllByTaxonomyExact(['by_locale', locale.toString(), tag1])
        List allBooksWithT2 = Book.findAllByTaxonomyExact(['by_locale', locale.toString(), tag2])

        then:
        setsAreEqual([b1], allBooksWithT1)
        setsAreEqual([b2], allBooksWithT2)

        where:
        locale | tag1  | translationLocale | translatedTag | tag2
        UK     | T1_UK | FRANCE            | T1_FR         | T2_UK
        UK     | T1_UK | SPAIN             | T1_ES         | T2_UK
        UK     | T2_UK | SPAIN             | T2_ES         | T1_UK
    }
    def 'finds several objects by a translated tag'() {
        setup:
        def b2 = new Book(name: 'b2').save()
        def b3 = new Book(name: 'b3').save()
        [b1, b2].each {b ->
            taggingService.tag(b, locale, tag)
            tagTranslationService.translateTag(b, locale, tag)
        }
        taggingService.tag(b3, locale, irrelevantTag)
        tagTranslationService.translateTag(b3, locale, irrelevantTag)


        when:
        List allBooks = Book.findAllByTaxonomyExact(['by_locale', translationLocale.toString(), translatedTag])
        assert allBooks.size() == 2

        then:
        setsAreEqual([b1, b2], allBooks)

        where:
        locale | tag   | translationLocale | translatedTag | irrelevantTag
        UK     | T1_UK | FRANCE            | T1_FR         | T2_UK
        UK     | T1_UK | SPAIN             | T1_ES         | T2_UK
        UK     | T2_UK | SPAIN             | T2_ES         | T1_UK
    }
}
