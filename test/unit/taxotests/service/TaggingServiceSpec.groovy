package taxotests.service

import grails.test.mixin.*
import grails.test.mixin.support.*

import spock.lang.Specification
import taxotests.Book
import taxotests.TagReference

import static java.util.Locale.FRANCE
import com.grailsrocks.taxonomy.Taxon
import com.grailsrocks.taxonomy.Taxonomy
import com.grailsrocks.taxonomy.TaxonLink
import com.grailsrocks.taxonomy.TaxonomyService
import static grails.test.MockUtils.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Book, TagReference, Taxon, Taxonomy, TaxonLink])
class TaggingServiceSpec extends Specification {
    def taggingService = new TaggingService()
    def taxonomyService = new TaxonomyService()

    def taxonomies = new TaxonomyHelper(taxonomyService)

    def b1
    static final T1 = 'T1'
    static final T2 = 'T2'

    private void hasExactlyOneTag(obj, String locale, String tag) {
        obj.hasTaxonomy(['by_locale', locale, tag])
        obj.taxonomies.size() == 1
    }


    def setup() {
        taxonomies.instrumentTaxonomyMethods([Book,TagReference])

        taxonomyService.init()
        mockLogging(TaxonomyService)

        b1 = new Book(name: 'b1').save()
    }

    def 'When an object is tagged, it is kept as a reference tag'() {
        when:
        taggingService.tag(b1, FRANCE, T1)

        then:
        TagReference.findByLocaleAndTag(FRANCE.toString(), T1)?.tag == T1
    }

    def 'When an object is tagged twice with the same tag, it is referenced only once'() {
        when:
        taggingService.tag(b1, FRANCE, T1)
        taggingService.tag(b1, FRANCE, T1)

        then:
        TagReference.findAllByLocaleAndTag(FRANCE.toString(), T1).size() == 1
    }

    def 'object is tagged and reference is tagged'() {
        when:
        taggingService.tag(b1, FRANCE, T1)
        def ref = TagReference.findByLocaleAndTag(FRANCE.toString(), T1)

        then:
        hasExactlyOneTag(b1, FRANCE.toString(), T1)
        hasExactlyOneTag(ref, FRANCE.toString(), T1)
    }

    def 'object is tagged with multiple tags'() {
        when:
        taggingService.tag(b1, FRANCE, T1)
        taggingService.tag(b1, FRANCE, T2)
        def ref1 = TagReference.findByLocaleAndTag(FRANCE.toString(), T1)
        def ref2 = TagReference.findByLocaleAndTag(FRANCE.toString(), T2)

        then:
        ref1 != null
        ref2 != null

        [T1, T2].every {tag ->
            b1.hasTaxonomy(['by_locale', 'fr_FR', tag])
        }

        hasExactlyOneTag(ref1,'fr_FR', T1)
        hasExactlyOneTag(ref2,'fr_FR', T2)
    }
}
