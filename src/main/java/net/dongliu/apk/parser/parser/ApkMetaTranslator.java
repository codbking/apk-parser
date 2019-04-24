package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.bean.*;
import net.dongliu.apk.parser.struct.ResourceValue;
import net.dongliu.apk.parser.struct.resource.Densities;
import net.dongliu.apk.parser.struct.resource.ResourceEntry;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.struct.resource.Type;
import net.dongliu.apk.parser.struct.xml.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * trans binary xml to text
 *
 * @author Liu Dong dongliu@live.cn
 */
public class ApkMetaTranslator implements XmlStreamer {
    private String[] tagStack = new String[100];
    private int depth = 0;
    private ApkMeta.Builder apkMetaBuilder = ApkMeta.newBuilder();
    private List<IconPath> iconPaths = Collections.emptyList();

    private ResourceTable resourceTable;
    @Nullable
    private Locale locale;

    public ApkMetaTranslator(ResourceTable resourceTable, @Nullable Locale locale) {
        this.resourceTable = Objects.requireNonNull(resourceTable);
        this.locale = locale;
    }

    @Override
    public void onStartTag(XmlNodeStartTag xmlNodeStartTag) {
        Attributes attributes = xmlNodeStartTag.getAttributes();
        switch (xmlNodeStartTag.getName()) {
            case "application":
                String label = attributes.getString("label");
                if (label != null) {
                    apkMetaBuilder.setLabel(label);
                }
                Attribute iconAttr = attributes.get("icon");
                if (iconAttr != null) {
                    ResourceValue resourceValue = iconAttr.getTypedValue();
                    if (resourceValue instanceof ResourceValue.ReferenceResourceValue) {
                        long resourceId = ((ResourceValue.ReferenceResourceValue) resourceValue).getReferenceResourceId();
                        List<ResourceTable.Resource> resources = this.resourceTable.getResourcesById(resourceId);
                        if (!resources.isEmpty()) {
                            List<IconPath> icons = new ArrayList<>();
                            boolean hasDefault = false;
                            for (ResourceTable.Resource resource : resources) {
                                Type type = resource.getType();
                                ResourceEntry resourceEntry = resource.getResourceEntry();
                                String path = resourceEntry.toStringValue(resourceTable, locale);
                                if (type.getDensity() == Densities.DEFAULT) {
                                    hasDefault = true;
                                    apkMetaBuilder.setIcon(path);
                                }
                                IconPath iconPath = new IconPath(path, type.getDensity());
                                icons.add(iconPath);
                            }
                            if (!hasDefault) {
                                apkMetaBuilder.setIcon(icons.get(0).getPath());
                            }
                            this.iconPaths = icons;
                        }
                    } else {
                        String value = iconAttr.getValue();
                        if (value != null) {
                            apkMetaBuilder.setIcon(value);
                            IconPath iconPath = new IconPath(value, Densities.DEFAULT);
                            this.iconPaths = Collections.singletonList(iconPath);
                        }
                    }
                }
                break;
            case "manifest":
                apkMetaBuilder.setPackageName(attributes.getString("package"));
                apkMetaBuilder.setVersionName(attributes.getString("versionName"));
                apkMetaBuilder.setVersionCode(attributes.getLong("versionCode"));
                String installLocation = attributes.getString("installLocation");
                if (installLocation != null) {
                    apkMetaBuilder.setInstallLocation(installLocation);
                }
                apkMetaBuilder.setCompileSdkVersion(attributes.getString("compileSdkVersion"));
                apkMetaBuilder.setCompileSdkVersionCodename(attributes.getString("compileSdkVersionCodename"));
                apkMetaBuilder.setPlatformBuildVersionCode(attributes.getString("platformBuildVersionCode"));
                apkMetaBuilder.setPlatformBuildVersionName(attributes.getString("platformBuildVersionName"));
                break;
            case "uses-sdk":
                apkMetaBuilder.setMinSdkVersion(attributes.getString("minSdkVersion"));
                apkMetaBuilder.setTargetSdkVersion(attributes.getString("targetSdkVersion"));
                apkMetaBuilder.setMaxSdkVersion(attributes.getString("maxSdkVersion"));
                break;
            case "supports-screens":
                apkMetaBuilder.setAnyDensity(attributes.getBoolean("anyDensity", false));
                apkMetaBuilder.setSmallScreens(attributes.getBoolean("smallScreens", false));
                apkMetaBuilder.setNormalScreens(attributes.getBoolean("normalScreens", false));
                apkMetaBuilder.setLargeScreens(attributes.getBoolean("largeScreens", false));
                break;
            case "uses-feature":
                String name = attributes.getString("name");
                boolean required = attributes.getBoolean("required", false);
                if (name != null) {
                    UseFeature useFeature = new UseFeature(name, required);
                    apkMetaBuilder.addUsesFeature(useFeature);
                } else {
                    Integer gl = attributes.getInt("glEsVersion");
                    if (gl != null) {
                        int v = gl;
                        GlEsVersion glEsVersion = new GlEsVersion(v >> 16, v & 0xffff, required);
                        apkMetaBuilder.setGlEsVersion(glEsVersion);
                    }
                }
                break;
            case "uses-permission":
                apkMetaBuilder.addUsesPermission(attributes.getString("name"));
                break;
            case "permission":
                Permission permission = new Permission(
                        attributes.getString("name"),
                        attributes.getString("label"),
                        attributes.getString("icon"),
                        attributes.getString("description"),
                        attributes.getString("group"),
                        attributes.getString("android:protectionLevel"));
                apkMetaBuilder.addPermissions(permission);
                break;
            case "meta-data":
                MetaData metaData = new MetaData(
                        attributes.getString("name"),
                        attributes.getString("value"),
                        attributes.getString("resource")
                );
                apkMetaBuilder.addMetaData(metaData);
                break;
        }
        tagStack[depth++] = xmlNodeStartTag.getName();
    }

    @Override
    public void onEndTag(XmlNodeEndTag xmlNodeEndTag) {
        depth--;
    }

    @Override
    public void onCData(XmlCData xmlCData) {

    }

    @Override
    public void onNamespaceStart(XmlNamespaceStartTag tag) {

    }

    @Override
    public void onNamespaceEnd(XmlNamespaceEndTag tag) {

    }

    @Nonnull
    public ApkMeta getApkMeta() {
        return apkMetaBuilder.build();
    }

    @Nonnull
    public List<IconPath> getIconPaths() {
        return iconPaths;
    }

    private boolean matchTagPath(String... tags) {
        // the root should always be "manifest"
        if (depth != tags.length + 1) {
            return false;
        }
        for (int i = 1; i < depth; i++) {
            if (!tagStack[i].equals(tags[i - 1])) {
                return false;
            }
        }
        return true;
    }

    private boolean matchLastTag(String tag) {
        // the root should always be "manifest"
        return tagStack[depth - 1].endsWith(tag);
    }
}
