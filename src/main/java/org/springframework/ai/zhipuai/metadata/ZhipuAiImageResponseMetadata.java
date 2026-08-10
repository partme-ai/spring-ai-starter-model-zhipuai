package org.springframework.ai.zhipuai.metadata;

import com.zhipu.oapi.service.v4.image.ImageResult;
import org.springframework.ai.image.ImageResponseMetadata;
import org.springframework.util.Assert;

import java.util.Objects;

public class ZhipuAiImageResponseMetadata extends ImageResponseMetadata {

    public static ZhipuAiImageResponseMetadata from(ImageResult imageResult) {
        Assert.notNull(imageResult, "ImageResult must not be null");
        return new ZhipuAiImageResponseMetadata(imageResult.getCreated());
    }

    protected ZhipuAiImageResponseMetadata(Long created) {
        super(created);
    }

    @Override
    public String toString() {
        return "ZhipuAiImageResponseMetadata{" + "created=" + getCreated() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ZhipuAiImageResponseMetadata that))
            return false;
        return Objects.equals(getCreated(), that.getCreated());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCreated());
    }

}
