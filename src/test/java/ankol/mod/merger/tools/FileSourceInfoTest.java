package ankol.mod.merger.tools;

import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/**
 * FileSourceInfo 单元测试
 * 验证文件来源链追踪功能
 */
public class FileSourceInfoTest {

    @Test
    public void testBasicSourceTracking() {
        Path testPath = Path.of("/tmp/test_file.scr");
        FileSourceInfo info = new FileSourceInfo(testPath);
        info.addSource("data3.pak");

        assertEquals(testPath, info.getFilePath());
        assertEquals("data3.pak", info.getSourceChainString());
        assertEquals("data3.pak", info.getPrimarySource());
        assertEquals("data3.pak", info.getDirectSource());
        assertFalse(info.isFromNestedArchive());
    }

    @Test
    public void testNestedSourceTracking() {
        Path testPath = Path.of("/tmp/test_file.scr");
        FileSourceInfo info = new FileSourceInfo(testPath);
        info.addSource("mymod.zip");
        info.addSource("data3.pak");

        assertEquals("mymod.zip -> data3.pak", info.getSourceChainString());
        assertEquals("mymod.zip", info.getPrimarySource());
        assertEquals("data3.pak", info.getDirectSource());
        assertTrue(info.isFromNestedArchive());

        List<String> chain = info.getSourceChain();
        assertEquals(2, chain.size());
        assertEquals("mymod.zip", chain.get(0));
        assertEquals("data3.pak", chain.get(1));
    }

    @Test
    public void testDeepNestedSourceTracking() {
        Path testPath = Path.of("/tmp/test_file.scr");
        FileSourceInfo info = new FileSourceInfo(testPath);
        info.addSource("container.zip");
        info.addSource("inner.pak");
        info.addSource("actual.zip");

        assertEquals("container.zip -> inner.pak -> actual.zip", info.getSourceChainString());
        assertEquals("container.zip", info.getPrimarySource());
        assertEquals("actual.zip", info.getDirectSource());
        assertTrue(info.isFromNestedArchive());
    }

    @Test
    public void testDuplicateSourceNotAdded() {
        Path testPath = Path.of("/tmp/test_file.scr");
        FileSourceInfo info = new FileSourceInfo(testPath);
        info.addSource("data3.pak");
        info.addSource("data3.pak");  // 重复

        List<String> chain = info.getSourceChain();
        assertEquals(1, chain.size());
        assertEquals("data3.pak", chain.get(0));
    }

    @Test
    public void testCopyConstructor() {
        Path testPath = Path.of("/tmp/test_file.scr");
        FileSourceInfo info1 = new FileSourceInfo(testPath);
        info1.addSource("mymod.zip");
        info1.addSource("data3.pak");

        FileSourceInfo info2 = new FileSourceInfo(info1);

        assertEquals(info1.getFilePath(), info2.getFilePath());
        assertEquals(info1.getSourceChainString(), info2.getSourceChainString());
        assertEquals(info1.getSourceChain(), info2.getSourceChain());

        // 修改 info2 不应该影响 info1
        info2.addSource("extra");
        assertNotEquals(info1.getSourceChain(), info2.getSourceChain());
    }

    @Test
    public void testEqualsAndHashCode() {
        Path testPath = Path.of("/tmp/test_file.scr");
        FileSourceInfo info1 = new FileSourceInfo(testPath);
        info1.addSource("mymod.zip");

        FileSourceInfo info2 = new FileSourceInfo(testPath);
        info2.addSource("mymod.zip");

        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    public void testToString() {
        Path testPath = Path.of("/tmp/test_file.scr");
        FileSourceInfo info = new FileSourceInfo(testPath);
        info.addSource("mymod.zip");
        info.addSource("data3.pak");

        String str = info.toString();
        assertTrue(str.contains("FileSourceInfo"));
        assertTrue(str.contains("mymod.zip -> data3.pak"));
    }
}

