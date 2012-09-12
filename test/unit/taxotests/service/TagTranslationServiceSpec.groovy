package taxotests.service

import com.grailsrocks.taxonomy.Taxon
import com.grailsrocks.taxonomy.TaxonLink
import com.grailsrocks.taxonomy.Taxonomy
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import taxotests.Book
import taxotests.TagReference

import static java.util.Locale.*
import com.grailsrocks.taxonomy.TaxonomyService

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

    def tagTranslationService
    def taggingService

    void setup() {
        new Initializer().initialize(
            ['tagTranslationService','taggingService'],
            this
        )
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
        taggingService.tag(b1, UK, T1_UK)

        when:
        tagTranslationService.translateTag(b1, UK, T1_UK)
        def ref = TagReference.findByLocaleAndTag(UK.toString(), T1_UK)

        then:
        def expected = [T1_UK, T1_ES, T1_FR] as Set
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
        setsAreEqual(
            [
                T1_UK, T1_ES, T1_FR,
                T2_UK, T2_ES, T2_FR
            ],
            allTags(b1)
        )
    }

    def 'translation attempt of unknown tag fails'() {
        setup:
        taggingService.tag(b1, GERMANY, T1_GERMAN)

        when:
        tagTranslationService.translateTag(b1, GERMANY, T1_GERMAN)

        then:
        setsAreEqual(allTags(b1), [T1_GERMAN])
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

    def 'translating reuses previously created tag references taxonomies'() {
        setup: 'if a book b1 has already been tagged and translated'
        def b2 = new Book(name: 'b2').save()
        taggingService.tag(b1, UK, T1_UK)
        taggingService.tag(b1, UK, T2_UK)
        tagTranslationService.translateAllTags(b1)

        when: 'and a second book b2 is tagged with a tag that has been translated for b1'
        taggingService.tag(b2, FRANCE, T1_FR)

        then: 'all relevant translated tags from b1 are applied to b2 without asking to translate'
        setsAreEqual(allTags(b2), [T1_ES, T1_FR, T1_UK])
        //only one tag reference exists that contains translated tags of the translation tree of T1
        //even if the translation process has been called twice
        TagReference.findAllByTaxonomyExact(['by_locale', SPAIN, T1_ES]).size() == 1
    }

    def 'translating with the wrong reference locale results in failure'() {
        setup:
        taggingService.tag(b1, UK, T1_UK)

        when:
        tagTranslationService.translateTag(b1, FRANCE, T1_FR)

        then:
        thrown(NullPointerException)
    }
}
