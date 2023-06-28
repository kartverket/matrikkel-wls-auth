package stuff;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Model;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Model
@Named("something")
public class SomeBean {
    private static final Logger logger = LogManager.getLogger(SomeBean.class);

    private BeanManager bm;

    @Inject
    public SomeBean(BeanManager bm) {
        this.bm = bm;
    }

    @Deprecated // proxy constructor
    SomeBean() {}

    public String getFoo() {
        return "42";
    }
}
